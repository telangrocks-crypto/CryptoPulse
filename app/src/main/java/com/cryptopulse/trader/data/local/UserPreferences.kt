package com.cryptopulse.trader.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.IOException
import java.security.GeneralSecurityException

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey: MasterKey
    private val prefs: EncryptedSharedPreferences

    init {
        try {
            masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            prefs = EncryptedSharedPreferences.create(
                context,
                "crypto_pulse_prefs_secure",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as EncryptedSharedPreferences
        } catch (e: GeneralSecurityException) {
            throw RuntimeException("Failed to initialize secure storage", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to initialize secure storage", e)
        }
    }

    var authToken: String?
        get() = prefs.getString("auth_token", null)
        set(value) = prefs.edit().putString("auth_token", value).apply()

    var accessToken: String?
        get() = prefs.getString("access_token", null)
        set(value) = prefs.edit().putString("access_token", value).apply()

    var refreshToken: String?
        get() = prefs.getString("refresh_token", null)
        set(value) = prefs.edit().putString("refresh_token", value).apply()

    var userId: String?
        get() = prefs.getString("user_id", null)
        set(value) = prefs.edit().putString("user_id", value).apply()

    var userEmail: String?
        get() = prefs.getString("user_email", null)
        set(value) = prefs.edit().putString("user_email", value).apply()

    var savedApiKey: String?
        get() = prefs.getString("api_key", null)
        set(value) = prefs.edit().putString("api_key", value).apply()

    var savedApiSecret: String?
        get() = prefs.getString("api_secret", null)
        set(value) = prefs.edit().putString("api_secret", value).apply()

    var savedExchange: String?
        get() = prefs.getString("exchange", "bybit")
        set(value) = prefs.edit().putString("exchange", value).apply()

    var savedEnv: String
        get() = prefs.getString("env", "prod") ?: "prod"
        set(value) = prefs.edit().putString("env", value).apply()

    fun getString(key: String, defaultValue: String?): String? {
        return prefs.getString(key, defaultValue)
    }

    fun putString(key: String, value: String?) {
        prefs.edit().putString(key, value).apply()
    }

    fun saveExchangeConfig(exchange: String, apiKey: String, apiSecret: String, env: String) {
        prefs.edit()
            .putString("exchange", exchange)
            .putString("api_key", apiKey)
            .putString("api_secret", apiSecret)
            .putString("env", env)
            .apply()
    }
}
