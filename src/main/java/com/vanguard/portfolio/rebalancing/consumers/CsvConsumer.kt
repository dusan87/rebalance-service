package com.vanguard.portfolio.rebalancing.consumers

interface CsvConsumer<T> {
    fun read(): List<T>
}
