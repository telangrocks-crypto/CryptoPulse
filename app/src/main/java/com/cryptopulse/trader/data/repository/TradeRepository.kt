package com.cryptopulse.trader.data.repository

import com.cryptopulse.trader.data.api.CryptoPulseApi
import com.cryptopulse.trader.data.api.TradeRequest
import com.cryptopulse.trader.data.api.TradeResponse
import com.cryptopulse.trader.data.model.LocalActiveTrade
import com.cryptopulse.trader.data.model.LocalPastTrade
import com.cryptopulse.trader.data.local.UserPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

@Singleton
class TradeRepository @Inject constructor(
    private val api: CryptoPulseApi,
    private val userPreferences: UserPreferences
) {
    private val gson = Gson()
    private val fileMutex = Mutex()

    suspend fun executeTrade(
        symbol: String, 
        side: String, 
        quantity: Double, 
        price: Double,
        investment_usdt: Double? = null,
        detectedEntry: Double? = null,
        detectedSl: Double? = null,
        detectedTp: Double? = null,
        action: String = "EXECUTE",
        signal_id: String? = null
    ): Result<TradeResponse> = withContext(Dispatchers.IO) {
        try {
            val token = userPreferences.authToken ?: return@withContext Result.failure(Exception("Not authenticated"))
            val user = userPreferences.userId ?: return@withContext Result.failure(Exception("User ID not found"))
            
            val request = TradeRequest(
                user_id = user,
                symbol = symbol.replace("/", ""),
                side = side.uppercase(),
                quantity = quantity,
                investment_usdt = investment_usdt,
                price = price,
                api_key = userPreferences.savedApiKey,
                api_secret = userPreferences.savedApiSecret,
                exchange = userPreferences.savedExchange ?: "bybit",
                detected_entry = detectedEntry,
                detected_sl = detectedSl,
                detected_tp = detectedTp,
                env = userPreferences.savedEnv,
                action = action,
                signal_id = signal_id ?: UUID.randomUUID().toString()
            )
            
            val response = api.executeTrade(token, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorJson = response.errorBody()?.string()
                val errorMessage = try {
                    if (!errorJson.isNullOrEmpty()) {
                        if (errorJson.startsWith("{")) {
                             val errorObj = gson.fromJson(errorJson, TradeResponse::class.java)
                             errorObj.message ?: "Trade execution failed: ${response.code()}"
                        } else {
                             errorJson
                        }
                    } else {
                        "Trade execution failed: ${response.code()}"
                    }
                } catch (e: Exception) {
                    "Trade execution failed: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun closePosition(
        tradeId: String,
        symbol: String? = null,
        side: String? = null,
        qty: Double? = null,
        slId: String? = null,
        tpId: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val token = userPreferences.authToken ?: return@withContext Result.failure(Exception("Not authenticated"))
            val user = userPreferences.userId ?: return@withContext Result.failure(Exception("User ID not found"))
            
            val localTrade = getLocalActiveTrades().find { it.tradeId == tradeId }
            
            val request = TradeRequest(
                user_id = user,
                trade_id = tradeId,
                symbol = symbol ?: localTrade?.symbol ?: return@withContext Result.failure(Exception("Symbol required to close position")),
                side = side ?: localTrade?.side ?: "Buy",
                quantity = qty ?: localTrade?.quantity ?: return@withContext Result.failure(Exception("Quantity required to close position")),
                sl_order_id = slId ?: localTrade?.stopLossOrderId,
                tp_order_id = tpId ?: localTrade?.takeProfitOrderId,
                price = 0.0,
                action = "CLOSE_POSITION",
                env = userPreferences.savedEnv,
                api_key = userPreferences.savedApiKey,
                api_secret = userPreferences.savedApiSecret,
                exchange = userPreferences.savedExchange ?: "bybit"
            )
            
            val response = api.executeTrade(token, request)
            if (response.isSuccessful) {
                removeLocalActiveTrade(tradeId)
                Result.success(true)
            } else {
                Result.failure(Exception("Close failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun closeAllPositions(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val trades = getLocalActiveTrades()
            var successCount = 0
            for (trade in trades) {
                val res = closePosition(
                    tradeId = trade.tradeId,
                    symbol = trade.symbol,
                    side = trade.side,
                    qty = trade.quantity,
                    slId = trade.stopLossOrderId,
                    tpId = trade.takeProfitOrderId
                )
                if (res.isSuccess) successCount++
            }
            Result.success(successCount == trades.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncActiveTrades(): Result<List<LocalActiveTrade>> = withContext(Dispatchers.IO) {
        try {
            val token = userPreferences.authToken ?: return@withContext Result.failure(Exception("Not authenticated"))
            val user = userPreferences.userId ?: return@withContext Result.failure(Exception("User ID not found"))
            
            val request = com.cryptopulse.trader.data.api.TradeListRequest(
                user_id = user,
                exchange = userPreferences.savedExchange ?: "bybit",
                env = userPreferences.savedEnv,
                api_key = userPreferences.savedApiKey,
                api_secret = userPreferences.savedApiSecret,
                action = "LIST_OPEN"
            )
            
            val response = api.getActiveTrades(token, request)
            
            if (response.isSuccessful && response.body() != null) {
                val apiTrades = response.body()!!.trades
                val localTrades = apiTrades.map { mapApiTradeToLocal(it) }
                
                saveLocalActiveTradesInternal(localTrades)
                Result.success(localTrades)
            } else {
                Result.failure(Exception("Failed to sync active trades: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getLocalActiveTrades(): List<LocalActiveTrade> = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            getLocalActiveTradesInternal()
        }
    }

    suspend fun addLocalActiveTrade(trade: LocalActiveTrade) {
        fileMutex.withLock {
            val list = getLocalActiveTradesInternal().toMutableList()
            list.add(trade)
            saveLocalActiveTradesInternal(list)
        }
    }

    suspend fun removeLocalActiveTrade(tradeId: String) {
        fileMutex.withLock {
            val list = getLocalActiveTradesInternal().toMutableList()
            list.removeAll { it.tradeId == tradeId }
            saveLocalActiveTradesInternal(list)
        }
    }

    private fun getLocalActiveTradesInternal(): List<LocalActiveTrade> {
        val json = userPreferences.getString("local_active_trades_list", null)
        return if (json != null) {
            try {
                val type = object : TypeToken<List<LocalActiveTrade>>() {}.type
                gson.fromJson(json, type)
            } catch (e: Exception) { emptyList() }
        } else {
            emptyList()
        }
    }

    private fun saveLocalActiveTradesInternal(list: List<LocalActiveTrade>) {
        val json = gson.toJson(list)
        userPreferences.putString("local_active_trades_list", json)
    }
    
    private fun mapApiTradeToLocal(apiTrade: com.cryptopulse.trader.data.api.ActiveTradeData): LocalActiveTrade {
        return LocalActiveTrade(
            tradeId = apiTrade.trade_id,
            signalId = apiTrade.signal_id,
            symbol = apiTrade.symbol,
            entryPrice = apiTrade.entry_price,
            stopLoss = apiTrade.sl_price ?: 0.0,
            takeProfit = apiTrade.tp_price ?: 0.0,
            side = apiTrade.side,
            timestamp = parseDateSafe(apiTrade.created_at),
            quantity = apiTrade.qty,
            unrealizedPnl = apiTrade.unrealized_pnl ?: 0.0,
            lastPrice = apiTrade.last_price ?: apiTrade.entry_price,
            stopLossOrderId = null, 
            takeProfitOrderId = null,
            isManualIntervention = false
        )
    }

    suspend fun syncPastTrades(): Result<List<LocalPastTrade>> = withContext(Dispatchers.IO) {
        try {
            val token = userPreferences.authToken ?: return@withContext Result.failure(Exception("Not authenticated"))
            val user = userPreferences.userId ?: return@withContext Result.failure(Exception("User ID not found"))
            
            val request = com.cryptopulse.trader.data.api.TradeListRequest(
                user_id = user,
                exchange = userPreferences.savedExchange ?: "binance",
                env = userPreferences.savedEnv,
                api_key = userPreferences.savedApiKey,
                api_secret = userPreferences.savedApiSecret,
                action = "LIST_HISTORY"
            )
            
            val response = api.getActiveTrades(token, request)
            
            if (response.isSuccessful && response.body() != null) {
                val apiTrades = response.body()!!.trades
                val localPastTrades = apiTrades.map { mapApiTradeToLocalPast(it) }
                
                fileMutex.withLock {
                    userPreferences.putString("local_past_trades_list", gson.toJson(localPastTrades))
                }
                Result.success(localPastTrades)
            } else {
                Result.failure(Exception("Failed to sync past trades: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPastTrades(): List<LocalPastTrade> = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            val json = userPreferences.getString("local_past_trades_list", null)
            if (json != null) {
                try {
                    val type = object : TypeToken<List<LocalPastTrade>>() {}.type
                    gson.fromJson(json, type)
                } catch (e: Exception) { emptyList() }
            } else {
                emptyList()
            }
        }
    }
    
    private fun mapApiTradeToLocalPast(apiTrade: com.cryptopulse.trader.data.api.ActiveTradeData): LocalPastTrade {
        return LocalPastTrade(
            tradeId = apiTrade.trade_id,
            symbol = apiTrade.symbol,
            side = apiTrade.side,
            entryPrice = apiTrade.entry_price,
            exitPrice = apiTrade.exit_price ?: apiTrade.entry_price, 
            finalPnl = apiTrade.realized_pnl ?: 0.0, 
            closeReason = apiTrade.status,
            exitReason = apiTrade.exit_reason ?: apiTrade.status,
            openTimestamp = parseDateSafe(apiTrade.created_at),
            closeTimestamp = if (apiTrade.closed_at != null) parseDateSafe(apiTrade.closed_at) else System.currentTimeMillis()
        )
    }

    private fun parseDateSafe(dateStr: String?): Long {
        if (dateStr.isNullOrEmpty()) return 0L
        return try {
            java.time.Instant.parse(dateStr).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    suspend fun addPastTrade(trade: LocalPastTrade) {
        fileMutex.withLock {
            val json = userPreferences.getString("local_past_trades_list", null)
            val list = if (json != null) {
                try {
                    val type = object : TypeToken<List<LocalPastTrade>>() {}.type
                    gson.fromJson<List<LocalPastTrade>>(json, type).toMutableList()
                } catch (e: Exception) { mutableListOf() }
            } else {
                mutableListOf()
            }
            list.add(0, trade) 
            userPreferences.putString("local_past_trades_list", gson.toJson(list))
        }
    }

    suspend fun validateExchange(
        apiKey: String,
        apiSecret: String,
        exchange: String,
        env: String
    ): Result<TradeResponse> = withContext(Dispatchers.IO) {
        try {
            val token = userPreferences.authToken ?: return@withContext Result.failure(Exception("Not authenticated"))
            val request = com.cryptopulse.trader.data.api.ValidationRequest(
                api_key = apiKey,
                api_secret = apiSecret,
                exchange = exchange,
                env = env
            )
            
            val response = api.validateExchange(token, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Exchange validation failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
