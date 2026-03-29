package com.cryptopulse.trader.util

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiagnosticsLogger @Inject constructor(
    @ApplicationContext val context: Context
) {
    enum class LogLevel {
        INFO, WARNING, ERROR
    }

    fun log(tag: String, message: String, level: LogLevel = LogLevel.INFO) {
        val formattedMessage = "[$tag] $message"
        when (level) {
            LogLevel.INFO -> Log.i("CryptoPulse", formattedMessage)
            LogLevel.WARNING -> Log.w("CryptoPulse", formattedMessage)
            LogLevel.ERROR -> Log.e("CryptoPulse", formattedMessage)
        }
    }
}
