package com.vanguard.portfolio.rebalancing.services

import com.vanguard.portfolio.rebalancing.consumers.CsvConsumer
import com.vanguard.portfolio.rebalancing.consumers.FinancialPortfolioConsumer
import com.vanguard.portfolio.rebalancing.consumers.FinancialPortfolioServiceException
import com.vanguard.portfolio.rebalancing.models.Customer
import com.vanguard.portfolio.rebalancing.models.Portfolio
import com.vanguard.portfolio.rebalancing.models.Strategy
import com.vanguard.portfolio.rebalancing.models.Trade
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class CustomerPortfolioRebalanceService(
    @Qualifier("customerReader")
    val customerClient: CsvConsumer<Customer>,
    @Qualifier("strategyReader")
    val strategyClient: CsvConsumer<Strategy>,
    private val financialPortfolioClient: FinancialPortfolioConsumer,
    @Value("\${financial.portfolio.service.api.bach.size}") val batchSize: Int
): Rebalancer {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private val fallbackStrategy = Strategy(
        strategyId = -1,
        minRiskLevel = -1,
        maxRiskLevel = -1,
        minYearsToRetirement = -1,
        maxYearsToRetirement = -1,
        stockPercentage = 0,
        cashPercentage = 100,
        bondsPercentage = 0
    )

    override fun rebalance() {
        customerClient
            .read()
            .mapNotNull { customer ->
                log.info("**** Rebalance customer $customer ****")
                val customerStrategy = matchCustomerStrategy(customer)
                log.info("1. Customer selected strategy $customerStrategy")
                val customerPortfolio = getCustomerPortfolio(customer)
                val customerTrades = calculateTrades(customerPortfolio, customerStrategy)
                log.info("3. Calculate trades $customerTrades \n\n")
                customerTrades
            }
            .chunked(batchSize)
            .forEach { batchTrades -> executeBatchTrades(batchTrades) }
    }

    private fun getCustomerPortfolio(customer: Customer): Portfolio? =
        try {
            log.info("2. Fetch portfolio for customer ${customer.customerId}")
            financialPortfolioClient.fetchPortfolio(customer.customerId)
        } catch (e: FinancialPortfolioServiceException) {
            log.error("FPS: Fetch customer portfolio request failed", e)
            null
        }

    private fun executeBatchTrades(tradesBatch: List<Trade>) {
        try {
            log.info("--> Sending batch trades $tradesBatch <--")
            financialPortfolioClient.execute(tradesBatch)
        } catch (e: FinancialPortfolioServiceException) {
            log.error("FPS: Rebalance trades request failed", e)
        }
    }

    private fun calculateTrades(portfolio: Portfolio?, strategy: Strategy) = if (portfolio != null) Trade(
        customerId = portfolio.customerId,
        stocks = (portfolio.totalAssets * (strategy.stockPercentage.toDouble() / 100)).toInt() - portfolio.stocks,
        bonds = (portfolio.totalAssets * (strategy.bondsPercentage.toDouble() / 100)).toInt() - portfolio.bonds,
        cash = (portfolio.totalAssets * (strategy.cashPercentage.toDouble() / 100)).toInt() - portfolio.cash,
    ) else null

    private fun matchCustomerStrategy(customer: Customer) = strategyClient.read()
        .firstOrNull { strategy -> customer.match(strategy) } ?: fallbackStrategy

    private fun Customer.match(strategy: Strategy) = (riskLevel in strategy.minRiskLevel..strategy.maxRiskLevel)
        && (yearsToRetirement in strategy.minYearsToRetirement..strategy.maxYearsToRetirement)
}
