package com.cryptopulse.trader.data.repository

import com.cryptopulse.trader.data.api.CryptoPulseApi
import com.cryptopulse.trader.data.api.MarketDataResponse
import com.cryptopulse.trader.data.local.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketRepository @Inject constructor(
    private val api: CryptoPulseApi,
    private val userPreferences: UserPreferences
) {
    suspend fun getMarketSummary(): Result<MarketDataResponse> = withContext(Dispatchers.IO) {
        try {
            val token = userPreferences.authToken ?: return@withContext Result.failure(Exception("Not authenticated"))
            val userId = userPreferences.userId ?: return@withContext Result.failure(Exception("User ID missing"))
            
            val response = api.getMarketData(
                token = token,
                symbols = null,
                userId = userId,
                exchange = userPreferences.savedExchange ?: "bybit",
                action = "summary",
                env = userPreferences.savedEnv
            )
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch market data: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
