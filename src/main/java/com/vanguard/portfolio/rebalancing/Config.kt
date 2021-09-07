package com.vanguard.portfolio.rebalancing

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("Demo")
open class Config {

    @Bean
    open fun setupMockServer() {
        val mockWebServer = MockWebServer()

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
                    "/customer/3" -> MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody("{\"customerId\": 3, \"stocks\": 400, \"cash\": 900, \"bonds\": 80}")
                    "/customer/4" -> MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody("{\"customerId\": 4, \"stocks\": 700, \"cash\": 700, \"bonds\": 10}")
                    "/execute" -> MockResponse()
                        .setResponseCode(201)
                        .addHeader("Content-Type", "application/json")
                    else -> MockResponse().setResponseCode(404)
                }
        }

        mockWebServer.start(8080)
    }
}
