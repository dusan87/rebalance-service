package com.vanguard.commons

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class CsvReaderTest {
    @Test
    fun `should csv file without header`() {
        val lines = CsvReader.readWithoutHeader("test.csv")

        assertThat(lines).isEqualTo(listOf("0,1"))
    }
}
