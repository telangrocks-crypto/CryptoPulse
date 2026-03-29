package com.cryptopulse.trader.data.model

data class MarketSignal(
    val id: String,
    val symbol: String,
    val type: String, // "BUY" or "SELL"
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val confidence: Double,
    val timestamp: Long,
    val quantity: Double,
    val reason: String? = null,
    val strategy: String? = null
)
