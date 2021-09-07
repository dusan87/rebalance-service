package com.vanguard.portfolio.rebalancing.consumers

import com.vanguard.portfolio.rebalancing.models.Portfolio
import com.vanguard.portfolio.rebalancing.models.Trade
import kotlin.jvm.Throws

interface FinancialPortfolioConsumer {
    @Throws(FinancialPortfolioServiceException::class)
    fun fetchPortfolio(customerId: Int): Portfolio?

    @Throws(FinancialPortfolioServiceException::class)
    fun execute(trades: List<Trade>)
}

class FinancialPortfolioServiceException : RuntimeException()
