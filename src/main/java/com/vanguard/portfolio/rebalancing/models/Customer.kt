package com.vanguard.portfolio.rebalancing.models

import java.time.LocalDate

data class Customer(
    val customerId: Int,
    val email: String,
    val dateOfBirth: LocalDate,
    val riskLevel: Int,
    val retirementAge: Int
) {
    // TODO (edge-case): already retired
    val yearsToRetirement: Int get() = retirementAge - (LocalDate.now().year - dateOfBirth.year)
}
