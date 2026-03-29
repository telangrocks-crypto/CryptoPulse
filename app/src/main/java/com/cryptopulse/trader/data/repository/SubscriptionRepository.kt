package com.cryptopulse.trader.data.repository

import com.cryptopulse.trader.data.api.CryptoPulseApi
import com.cryptopulse.trader.data.api.SubscriptionResponse
import com.cryptopulse.trader.data.local.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    private val api: CryptoPulseApi,
    private val userPreferences: UserPreferences
) {
    suspend fun getSubscriptionStatus(): Result<SubscriptionResponse> = withContext(Dispatchers.IO) {
        try {
            val token = userPreferences.authToken ?: return@withContext Result.failure(Exception("Not authenticated"))
            val userId = userPreferences.userId ?: return@withContext Result.failure(Exception("User ID missing"))
            
            val response = api.getSubscription(token, userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Subscription check failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
