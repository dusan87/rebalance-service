package com.vanguard.portfolio.rebalancing.services

import com.vanguard.portfolio.rebalancing.consumers.CustomerReader
import com.vanguard.portfolio.rebalancing.consumers.FinancialPortfolioServiceClient
import com.vanguard.portfolio.rebalancing.consumers.StrategyReader
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CustomerPortfolioRebalanceServiceIntegrationTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var financialPortfolioServiceClient: FinancialPortfolioServiceClient
    private val customerReader: CustomerReader = CustomerReader()
    private val strategyReader: StrategyReader = StrategyReader()
    private lateinit var subject: CustomerPortfolioRebalanceService

    @BeforeEach
    fun initialize() {
        mockWebServer = MockWebServer()
        mockWebServer.start(8989)
        val baseUrl = "http://localhost:${mockWebServer.port}"
        financialPortfolioServiceClient = FinancialPortfolioServiceClient(baseUrl)

        subject = CustomerPortfolioRebalanceService(
            customerReader, strategyReader, financialPortfolioServiceClient, batchSize = 50)
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `should rebalance customer portfolio`() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                when (request.path) {
                    "/customer/1" -> MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody("{\"customerId\": 1, \"stocks\": 10, \"cash\": 20, \"bonds\": 30}")
                    "/customer/2" -> MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody("{\"customerId\": 2, \"stocks\": 100, \"cash\": 10, \"bonds\": 80}")
                    else -> MockResponse()
                }
        }

        subject.rebalance()

        val customerOnePortfolioRequest = mockWebServer.takeRequest()
        assertThat(customerOnePortfolioRequest.path).isEqualTo("/customer/1")
        assertThat(customerOnePortfolioRequest.method).isEqualTo("GET")

        val customerTwoPortfolioRequest = mockWebServer.takeRequest()
        assertThat(customerTwoPortfolioRequest.path).isEqualTo("/customer/2")

        val executeTradesRequest = mockWebServer.takeRequest()
        assertThat(executeTradesRequest.body.readUtf8()).isEqualTo(
            "[{\"customerId\":1,\"stocks\":-4,\"cash\":-8,\"bonds\":12}," +
            "{\"customerId\":2,\"stocks\":-100,\"cash\":180,\"bonds\":-80}]"
        )
    }
}
