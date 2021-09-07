package com.vanguard.portfolio.rebalancing.models

data class Strategy(
    val strategyId: Int,
    val minRiskLevel: Int,
    val maxRiskLevel: Int,
    val minYearsToRetirement: Int,
    val maxYearsToRetirement: Int,
    val stockPercentage: Int,
    val cashPercentage: Int,
    val bondsPercentage: Int
)
