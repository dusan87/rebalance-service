package com.vanguard.portfolio.rebalancing.consumers

import com.vanguard.commons.CsvReader
import com.vanguard.portfolio.rebalancing.models.Strategy
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class StrategyReaderTest {

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should map strategies from file`() {
        mockkObject(CsvReader)
        every { CsvReader.readWithoutHeader("strategies.csv") } returns listOf("1,100,999,20,40,60,10,30")

        val strategies = StrategyReader().read()

        val expectedStrategy = Strategy(
            strategyId = 1,
            minRiskLevel = 100,
            maxRiskLevel = 999,
            minYearsToRetirement = 20,
            maxYearsToRetirement = 40,
            stockPercentage = 60,
            cashPercentage = 10,
            bondsPercentage = 30
        )
        Assertions.assertThat(strategies[0]).isEqualTo(expectedStrategy)
    }
}
