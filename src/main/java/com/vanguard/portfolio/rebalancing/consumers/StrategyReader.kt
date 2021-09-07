package com.vanguard.portfolio.rebalancing.consumers

import com.vanguard.commons.CsvReader
import com.vanguard.portfolio.rebalancing.models.Strategy
import org.springframework.stereotype.Component

@Component("strategyReader")
class StrategyReader : CsvConsumer<Strategy> {
    override fun read(): List<Strategy> = CsvReader
        .readWithoutHeader("strategies.csv")
        .map {
            val strategy = it.split(",")
            Strategy(
                strategyId = strategy[0].toInt(),
                minRiskLevel = strategy[1].toInt(),
                maxRiskLevel = strategy[2].toInt(),
                minYearsToRetirement = strategy[3].toInt(),
                maxYearsToRetirement = strategy[4].toInt(),
                stockPercentage = strategy[5].toInt(),
                cashPercentage = strategy[6].toInt(),
                bondsPercentage = strategy[7].toInt()
            )
        }
}
