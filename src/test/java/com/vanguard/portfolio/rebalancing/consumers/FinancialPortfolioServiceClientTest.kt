package com.vanguard.portfolio.rebalancing.consumers

import com.vanguard.portfolio.rebalancing.models.Portfolio
import com.vanguard.portfolio.rebalancing.models.Trade
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.concurrent.TimeUnit


// TODO: (edge-case) security for API endpoint not applied
class FinancialPortfolioServiceClientTest {

    private lateinit var mockWebServer: MockWebServer

    private lateinit var financialPortfolioServiceClient: FinancialPortfolioServiceClient

    @BeforeEach
    fun initialize() {
        mockWebServer = MockWebServer()
        mockWebServer.start(1111)
        val baseUrl = "http://localhost:${mockWebServer.port}"
        financialPortfolioServiceClient = FinancialPortfolioServiceClient(baseUrl)
    }

    @Test
    fun `should fetch customer portfolio by customer id`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"customerId\": 100, \"stocks\": 10, \"cash\": 20, \"bonds\": 30}")
        )

        val customerPortfolio = financialPortfolioServiceClient.fetchPortfolio(100)

        assertThat(customerPortfolio).isEqualTo(Portfolio(100, 10, 20, 30))
    }

    @Test
    fun `should throw exception when request fetching portfolio fails`() {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(503)
        )

        assertThrows<FinancialPortfolioServiceException> {
            financialPortfolioServiceClient.fetchPortfolio(100)
        }
    }

    @Test
    fun `should execute customer rebalanced trades`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"customerId\": 100, \"stocks\": 10, \"cash\": 20, \"bonds\": 30}")
        )

        financialPortfolioServiceClient.execute(listOf(Trade(0, 0, 0, 0)))

        val recordedRequest: RecordedRequest = mockWebServer.takeRequest()

        assertThat(recordedRequest.body.readUtf8()).isEqualTo("[{\"customerId\":0,\"stocks\":0,\"cash\":0,\"bonds\":0}]")
    }

    @Test
    fun `should throw exception when request executing trades fails`() {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(503)
        )

        assertThrows<FinancialPortfolioServiceException> {
            financialPortfolioServiceClient.execute(listOf())
        }
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }
}
