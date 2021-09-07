package com.vanguard.portfolio.rebalancing.services

import com.vanguard.portfolio.rebalancing.consumers.CsvConsumer
import com.vanguard.portfolio.rebalancing.consumers.FinancialPortfolioConsumer
import com.vanguard.portfolio.rebalancing.consumers.FinancialPortfolioServiceException
import com.vanguard.portfolio.rebalancing.models.Customer
import com.vanguard.portfolio.rebalancing.models.Portfolio
import com.vanguard.portfolio.rebalancing.models.Strategy
import com.vanguard.portfolio.rebalancing.models.Trade
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Qualifier
import java.time.LocalDate


@ExtendWith(MockitoExtension::class)
class CustomerPortfolioRebalanceServiceTest {

    private fun <T> any(): T = Mockito.any()

    private lateinit var subject: CustomerPortfolioRebalanceService

    @Mock
    @Qualifier("customerReader")
    lateinit var customerReader: CsvConsumer<Customer>

    @Mock
    @Qualifier("strategyReader")
    lateinit var strategyReader: CsvConsumer<Strategy>

    @Mock
    @Qualifier("fpsClient")
    lateinit var financialPortfolioClient: FinancialPortfolioConsumer

    @BeforeEach
    fun setUp() {
        subject = CustomerPortfolioRebalanceService(
            customerReader,
            strategyReader,
            financialPortfolioClient,
            1
        )
    }

    @Test
    fun `should get customers to rebalance assets`() {

        subject.rebalance()

        Mockito.verify(customerReader, Mockito.times(1)).read()
    }

    @Test
    fun `should read strategies to select customer rebalance strategy`() {
        `when`(customerReader.read()).thenReturn(listOf())

        subject.rebalance()

        Mockito.verifyNoInteractions(financialPortfolioClient)
        Mockito.verifyNoInteractions(strategyReader)
    }

    @Test
    fun `should fetch customer portfolio from financial portfolio service`() {
        val customer = Customer(100, "test@email", LocalDate.now(), 1, 65)
        `when`(customerReader.read()).thenReturn(listOf(customer))
        `when`(strategyReader.read()).thenReturn(listOf())
        `when`(financialPortfolioClient.fetchPortfolio(100)).thenReturn(Portfolio(100, 0, 0, 0))

        subject.rebalance()

        Mockito.verify(financialPortfolioClient, Mockito.times(1)).fetchPortfolio(customer.customerId)
    }

    @Test
    fun `should execute trades to financial portfolio service when strategy selected for customer`() {
        val customer = Customer(
            customerId = 100, email = "test@email",
            dateOfBirth = LocalDate.of(1987, 10, 10),
            riskLevel = 3, retirementAge = 65)
        val strategy = Strategy(
            strategyId = 0, minRiskLevel = 1, maxRiskLevel = 5, minYearsToRetirement = 20,
            maxYearsToRetirement = 40, stockPercentage = 10, cashPercentage = 20, bondsPercentage = 70)
        val customerPortfolio = Portfolio(
            customerId = 100, stocks = 10, cash = 4, bonds = 6)

        `when`(customerReader.read()).thenReturn(listOf(customer))
        `when`(strategyReader.read()).thenReturn(listOf(strategy))
        `when`(financialPortfolioClient.fetchPortfolio(100)).thenReturn(customerPortfolio)

        subject.rebalance()

        Mockito.verify(financialPortfolioClient).execute(listOf(Trade(100, -8, 0, 8)))
    }

    @Test
    fun `should execute trades to financial portfolio service with fallback strategy when no strategy exists for customer risk level`() {
        val customer = Customer(
            customerId = 100, email = "test@email",
            dateOfBirth = LocalDate.of(1987, 10, 10),
            riskLevel = 100, retirementAge = 65)
        val strategy = Strategy(
            strategyId = 0, minRiskLevel = 1, maxRiskLevel = 5, minYearsToRetirement = 20,
            maxYearsToRetirement = 40, stockPercentage = 10, cashPercentage = 20, bondsPercentage = 70)
        val customerPortfolio = Portfolio(
            customerId = 100, stocks = 10, cash = 4, bonds = 6)

        `when`(customerReader.read()).thenReturn(listOf(customer))
        `when`(strategyReader.read()).thenReturn(listOf(strategy))
        `when`(financialPortfolioClient.fetchPortfolio(100)).thenReturn(customerPortfolio)

        subject.rebalance()

        Mockito.verify(financialPortfolioClient, Mockito.times(1)).execute(listOf(Trade(100, -10, 16, -6)))
    }

    @Test
    fun `should execute trades to financial portfolio service with fallback strategy when no strategy exists for customer years to retirement`() {
        val customer = Customer(
            customerId = 100, email = "test@email",
            dateOfBirth = LocalDate.of(1987, 10, 10),
            riskLevel = 3, retirementAge = 65)
        val strategy = Strategy(
            strategyId = 0, minRiskLevel = 1, maxRiskLevel = 5, minYearsToRetirement = 40,
            maxYearsToRetirement = 45, stockPercentage = 10, cashPercentage = 20, bondsPercentage = 70)
        val customerPortfolio = Portfolio(
            customerId = 100, stocks = 10, cash = 4, bonds = 6)

        `when`(customerReader.read()).thenReturn(listOf(customer))
        `when`(strategyReader.read()).thenReturn(listOf(strategy))
        `when`(financialPortfolioClient.fetchPortfolio(100)).thenReturn(customerPortfolio)

        subject.rebalance()

        Mockito.verify(financialPortfolioClient, Mockito.times(1)).execute(listOf(Trade(100, -10, 16, -6)))
    }

    @Test
    fun `should execute trades to financial portfolio service in batches`() {
        val customerBatch1 = Customer(
            customerId = 100, email = "test1@email",
            dateOfBirth = LocalDate.of(1987, 10, 10),
            riskLevel = 3, retirementAge = 65)
        val customerBatch2 = Customer(
            customerId = 101, email = "test2@email",
            dateOfBirth = LocalDate.of(1987, 10, 10),
            riskLevel = 5, retirementAge = 65)
        val strategy = Strategy(
            strategyId = 0, minRiskLevel = 1, maxRiskLevel = 5, minYearsToRetirement = 20,
            maxYearsToRetirement = 40, stockPercentage = 10, cashPercentage = 20, bondsPercentage = 70)
        val customerBatch1Portfolio = Portfolio(
            customerId = 100, stocks = 10, cash = 4, bonds = 6)
        val customerBatch2Portfolio = Portfolio(
            customerId = 101, stocks = 10, cash = 4, bonds = 6)

        `when`(customerReader.read()).thenReturn(listOf(customerBatch1, customerBatch2))
        `when`(strategyReader.read()).thenReturn(listOf(strategy))
        `when`(financialPortfolioClient.fetchPortfolio(100)).thenReturn(customerBatch1Portfolio)
        `when`(financialPortfolioClient.fetchPortfolio(101)).thenReturn(customerBatch2Portfolio)

        subject.rebalance()

        Mockito.verify(financialPortfolioClient, Mockito.times(2)).execute(any())
    }

    @Test
    fun `should not execute trades for a customer when fetching portfolio fails`() {
        val customer = Customer(
            customerId = 100, email = "test@email",
            dateOfBirth = LocalDate.of(1987, 10, 10),
            riskLevel = 100, retirementAge = 65)
        val strategy = Strategy(
            strategyId = 0, minRiskLevel = 1, maxRiskLevel = 5, minYearsToRetirement = 20,
            maxYearsToRetirement = 40, stockPercentage = 10, cashPercentage = 20, bondsPercentage = 70)

        `when`(customerReader.read()).thenReturn(listOf(customer))
        `when`(strategyReader.read()).thenReturn(listOf(strategy))
        `when`(financialPortfolioClient.fetchPortfolio(100)).thenThrow(FinancialPortfolioServiceException())

        subject.rebalance()

        Mockito.verifyNoMoreInteractions(financialPortfolioClient)
    }

    @Test
    fun `should do nothing WHEN customers empty`() {
        `when`(customerReader.read()).thenReturn(listOf())

        subject.rebalance()

        Mockito.verifyNoInteractions(financialPortfolioClient)
        Mockito.verifyNoInteractions(strategyReader)
    }
}
