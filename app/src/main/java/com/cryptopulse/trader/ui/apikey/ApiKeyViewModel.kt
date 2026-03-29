package com.cryptopulse.trader.ui.apikey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptopulse.trader.data.api.TradeResponse
import com.cryptopulse.trader.data.local.UserPreferences
import com.cryptopulse.trader.data.repository.TradeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApiKeyViewModel @Inject constructor(
    private val tradeRepository: TradeRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _validationResult = MutableSharedFlow<Result<TradeResponse>>()
    val validationResult = _validationResult.asSharedFlow()

    private val _savedConfig = MutableStateFlow<ApiKeyConfig?>(null)
    val savedConfig = _savedConfig.asStateFlow()

    init {
        loadConfig()
    }

    private fun loadConfig() {
        val key = userPreferences.savedApiKey
        val secret = userPreferences.savedApiSecret
        val exchange = userPreferences.savedExchange ?: "bybit"
        val env = userPreferences.savedEnv

        if (!key.isNullOrEmpty()) {
            _savedConfig.value = ApiKeyConfig(exchange, key, secret ?: "", env)
        }
    }

    fun validateAndSave(exchange: String, key: String, secret: String, env: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = tradeRepository.validateExchange(key, secret, exchange, env)
            
            result.onSuccess {
                userPreferences.saveExchangeConfig(exchange, key, secret, env)
                _savedConfig.value = ApiKeyConfig(exchange, key, secret, env)
            }
            
            _validationResult.emit(result)
            _isLoading.value = false
        }
    }

    data class ApiKeyConfig(
        val exchange: String,
        val apiKey: String,
        val apiSecret: String,
        val env: String
    )
}
