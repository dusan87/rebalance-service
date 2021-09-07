package com.vanguard.portfolio.rebalancing.consumers

import com.vanguard.portfolio.rebalancing.models.Portfolio
import com.vanguard.portfolio.rebalancing.models.Trade
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component("fpsClient")
class FinancialPortfolioServiceClient(
    @Value("\${financial.portfolio.service.baseUrl}") val baseUrl: String
) : FinancialPortfolioConsumer {

    private val webClient: WebClient = getWebClient()

    private fun getWebClient(): WebClient {
        return WebClient.builder().baseUrl(baseUrl)
            .defaultHeader("Content-Type", "application/json")
            .build()
    }

    override fun fetchPortfolio(customerId: Int): Portfolio? = webClient
        .get()
        .uri("/customer/${customerId}")
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .onStatus({ status -> status.isError }, { Mono.just(FinancialPortfolioServiceException()) })
        .bodyToMono(Portfolio::class.java)
        .block()

    override fun execute(trades: List<Trade>) {
        webClient
            .post()
            .uri("/execute")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(trades)
            .retrieve()
            .onStatus({ status -> status.isError }, { Mono.just(FinancialPortfolioServiceException()) })
            .bodyToMono(Void::class.java)
            .block()
    }

}
