package com.vanguard.portfolio.rebalancing.consumers

import com.vanguard.commons.CsvReader
import com.vanguard.portfolio.rebalancing.models.Customer
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CustomerReaderTest {

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should map customers from file`() {
        mockkObject(CsvReader)
        every { CsvReader.readWithoutHeader("customers.csv") } returns listOf("1,test@test.com,3,1961-04-29,65")

        val customers = CustomerReader().read()

        val expectedCustomer = Customer(
            1,
            "test@test.com",
            LocalDate.of(1961, 4, 29),
            3,
            65
        )
        assertThat(customers[0]).isEqualTo(expectedCustomer)
    }
}
