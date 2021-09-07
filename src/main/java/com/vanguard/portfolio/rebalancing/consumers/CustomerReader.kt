package com.vanguard.portfolio.rebalancing.consumers

import com.vanguard.commons.CsvReader
import com.vanguard.portfolio.rebalancing.models.Customer
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component("customerReader")
class CustomerReader : CsvConsumer<Customer> {
    override fun read(): List<Customer> = CsvReader
        .readWithoutHeader("customers.csv")
        .map {
            val customer = it.split(",")
            val customerId = customer[0].toInt()
            val customerEmail = customer[1]
            val customerRiskLevel = customer[2].toInt()
            val customerDateOfBirth = LocalDate.parse(customer[3])
            val customerRetirementAge = customer[4].toInt()

            Customer(customerId, customerEmail, customerDateOfBirth , customerRiskLevel, customerRetirementAge)
        }
}
