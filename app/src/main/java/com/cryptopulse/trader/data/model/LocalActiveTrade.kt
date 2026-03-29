package com.cryptopulse.trader.data.model

data class LocalActiveTrade(
    val tradeId: String,
    val signalId: String?,
    val symbol: String,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val side: String,
    val timestamp: Long,
    val quantity: Double,
    val unrealizedPnl: Double = 0.0,
    val lastPrice: Double = 0.0,
    val stopLossOrderId: String? = null,
    val takeProfitOrderId: String? = null,
    val isManualIntervention: Boolean = false
)
