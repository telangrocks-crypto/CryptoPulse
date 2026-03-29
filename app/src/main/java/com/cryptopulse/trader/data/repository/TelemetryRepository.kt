package com.cryptopulse.trader.data.repository

import android.os.Build
import com.cryptopulse.trader.data.api.CryptoPulseApi
import com.cryptopulse.trader.data.api.TelemetryRequest
import com.cryptopulse.trader.data.api.TelemetryResponse
import com.cryptopulse.trader.data.local.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryRepository @Inject constructor(
    private val api: CryptoPulseApi,
    private val userPreferences: UserPreferences
) {
    suspend fun sendPulse(status: String = "ACTIVE"): Result<TelemetryResponse> = withContext(Dispatchers.IO) {
        try {
            val token = userPreferences.authToken ?: return@withContext Result.failure(Exception("Not authenticated"))
            val userId = userPreferences.userId ?: return@withContext Result.failure(Exception("User ID missing"))
            
            val startTime = System.currentTimeMillis()
            
            val deviceInfo = mutableMapOf(
                "model" to Build.MODEL,
                "manufacturer" to Build.MANUFACTURER,
                "sdk" to Build.VERSION.SDK_INT.toString(),
                "os" to "Android"
            )

            val request = TelemetryRequest(
                user_id = userId,
                status = status,
                latency_ms = 0, // Server will calculate if needed, or update periodically
                app_version = "1.1.0-STABLE",
                device_info = deviceInfo
            )

            val requestWithLatency = request.copy(
                latency_ms = System.currentTimeMillis() - startTime
            )

            val response = api.sendTelemetry(token, requestWithLatency)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Telemetry failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
