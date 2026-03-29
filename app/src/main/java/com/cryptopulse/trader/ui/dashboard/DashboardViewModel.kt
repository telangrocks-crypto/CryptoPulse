package com.cryptopulse.trader.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptopulse.trader.data.api.MarketData
import com.cryptopulse.trader.data.api.SubscriptionResponse
import com.cryptopulse.trader.data.model.MarketSignal
import com.cryptopulse.trader.data.model.LocalActiveTrade
import com.cryptopulse.trader.data.repository.MarketRepository
import com.cryptopulse.trader.data.repository.TradeRepository
import com.cryptopulse.trader.data.repository.AuthRepository
import com.cryptopulse.trader.data.repository.SubscriptionRepository
import com.cryptopulse.trader.data.local.UserPreferences
import com.cryptopulse.trader.data.service.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val marketRepository: MarketRepository,
    private val tradeRepository: TradeRepository,
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val userPreferences: UserPreferences,
    private val notificationService: NotificationService
) : ViewModel() {

    private val _marketData = MutableStateFlow<List<MarketData>>(emptyList())
    val marketData = _marketData.asStateFlow()

    private val _activeTrades = MutableStateFlow<List<LocalActiveTrade>>(emptyList())
    val activeTrades = _activeTrades.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _subscriptionStatus = MutableStateFlow<SubscriptionResponse?>(null)
    val subscriptionStatus = _subscriptionStatus.asStateFlow()

    private val _exchangeBalance = MutableStateFlow<Double?>(null)
    val exchangeBalance = _exchangeBalance.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DashboardEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun fetchData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Market summary from backend (live data only)
                marketRepository.getMarketSummary().onSuccess { data ->
                    _marketData.value = data.results
                }

                // 2. Active trades from exchange via backend
                tradeRepository.syncActiveTrades().onSuccess { trades ->
                    _activeTrades.value = trades
                    // Update exchange balance from trade data if available
                    val totalPnl = trades.sumOf { it.unrealizedPnl }
                    if (totalPnl != 0.0) {
                        _exchangeBalance.value = totalPnl
                    }
                }

                // 3. Subscription status
                subscriptionRepository.getSubscriptionStatus().onSuccess { sub ->
                    _subscriptionStatus.value = sub
                }

            } catch (e: Exception) {
                _uiEvent.emit(DashboardEvent.ShowError("Data load failed: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSetEntryTapped() {
        viewModelScope.launch {
            val selectedPair = _marketData.value.firstOrNull()
            _uiEvent.emit(DashboardEvent.OpenSetEntry(selectedPair))
        }
    }

    fun onSimulateTrade() {
        viewModelScope.launch {
            val selectedPair = _marketData.value.firstOrNull()
            _uiEvent.emit(DashboardEvent.OpenSimulateTrade(selectedPair))
        }
    }

    fun executeTrade(signal: MarketSignal) {
        viewModelScope.launch {
            tradeRepository.executeTrade(
                symbol = signal.symbol,
                side = signal.type,
                quantity = signal.quantity,
                price = signal.entryPrice,
                signal_id = signal.id
            ).onSuccess {
                notificationService.showTradeNotification(
                    title = "Trade Executed",
                    message = "${signal.type} ${signal.symbol} @ ${signal.entryPrice}"
                )
                fetchData()
            }.onFailure {
                _uiEvent.emit(DashboardEvent.ShowError("Trade failed: ${it.message}"))
            }
        }
    }

    fun closePosition(tradeId: String) {
        viewModelScope.launch {
            tradeRepository.closePosition(tradeId).onSuccess {
                fetchData()
            }.onFailure {
                _uiEvent.emit(DashboardEvent.ShowError("Close failed: ${it.message}"))
            }
        }
    }

    sealed class DashboardEvent {
        data class ShowError(val message: String) : DashboardEvent()
        data class OpenSetEntry(val market: MarketData?) : DashboardEvent()
        data class OpenSimulateTrade(val market: MarketData?) : DashboardEvent()
    }
}
