package com.cryptopulse.trader.data.model

data class LocalPastTrade(
    val tradeId: String,
    val symbol: String,
    val side: String,
    val entryPrice: Double,
    val exitPrice: Double,
    val finalPnl: Double,
    val closeReason: String?,
    val exitReason: String?,
    val openTimestamp: Long,
    val closeTimestamp: Long
)
