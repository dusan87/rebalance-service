package com.vanguard.portfolio.rebalancing.models

import com.fasterxml.jackson.annotation.JsonIgnore

data class Assets(
    val customerId: Int,
    val stocks: Int,
    val cash: Int,
    val bonds: Int)
{
    constructor(): this(0, 0, 0, 0)

    @get:JsonIgnore
    val totalAssets: Int get() = stocks + cash + bonds
}

typealias Portfolio = Assets
typealias Trade = Assets
