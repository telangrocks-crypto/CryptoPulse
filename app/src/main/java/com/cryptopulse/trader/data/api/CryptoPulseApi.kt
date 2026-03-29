package com.cryptopulse.trader.data.api

import retrofit2.Response
import retrofit2.http.*

interface CryptoPulseApi {
    
    @POST("auth")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("auth")
    suspend fun verify(@Body request: VerifyRequest): Response<AuthResponse>
    
    @POST("auth")
    suspend fun resendSms(@Body request: ResendRequest): Response<AuthResponse>
    
    @POST("auth")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("auth")
    suspend fun refreshToken(@Body request: RefreshRequest): Response<AuthResponse>

    @POST("auth")
    suspend fun logout(@Body request: LogoutRequest): Response<AuthResponse>

    @POST("auth")
    suspend fun fetchTestOtp(@Body request: OtpFetchRequest): Response<OtpFetchResponse>

    @POST("auth/device-token")
    suspend fun updateDeviceToken(
        @Header("Authorization") token: String,
        @Body request: DeviceTokenRequest
    ): Response<AuthResponse>
    
    @GET("market-data")
    suspend fun getMarketData(
        @Header("Authorization") token: String,
        @Query("symbols") symbols: String?,
        @Query("user_id") userId: String,
        @Query("exchange") exchange: String,
        @Query("action") action: String?,
        @Query("env") env: String,
        @Query("api_key") apiKey: String? = null,
        @Query("api_secret") apiSecret: String? = null,
        @Query("capital") capital: Double? = null
    ): Response<MarketDataResponse>
    
    @GET("market-data")
    suspend fun getMarketDataForPair(
        @Header("Authorization") token: String,
        @Query("symbols") symbol: String,
        @Query("user_id") userId: String,
        @Query("exchange") exchange: String,
        @Query("timeframe") timeframe: String,
        @Query("action") action: String = "chart",
        @Query("env") env: String,
        @Query("api_key") apiKey: String? = null,
        @Query("api_secret") apiSecret: String? = null
    ): Response<MarketDataResponse>
    
    @GET("trade-signals")
    suspend fun getTradeSignals(
        @Header("Authorization") token: String,
        @Query("symbol") symbol: String,
        @Query("user_id") userId: String
    ): Response<TradeSignalsResponse>
    
    @POST("trades")
    suspend fun executeTrade(
        @Header("Authorization") token: String,
        @Body request: TradeRequest
    ): Response<TradeResponse>

    @POST("trades")
    suspend fun getActiveTrades(
        @Header("Authorization") token: String,
        @Body request: TradeListRequest
    ): Response<ActiveTradesResponse>
    
    @GET("subscriptions")
    suspend fun getSubscription(
        @Header("Authorization") token: String,
        @Query("user_id") userId: String
    ): Response<SubscriptionResponse>
    
    @POST("ai-strategy")
    suspend fun evaluateStrategy(
        @Header("Authorization") token: String,
        @Body request: StrategyEvaluationRequest
    ): Response<MarketDataResponse>

    @POST("ai-strategy")
    suspend fun scanMarket(
        @Header("Authorization") token: String,
        @Body request: StrategyEvaluationRequest
    ): Response<MarketDataResponse>

    @POST("payments")
    suspend fun verifyPurchase(
        @Header("Authorization") token: String,
        @Body request: VerifyPurchaseRequest
    ): Response<VerifyPurchaseResponse>

    @POST("exchange-validator")
    suspend fun validateExchange(
        @Header("Authorization") token: String,
        @Body request: ValidationRequest
    ): Response<TradeResponse>

    @POST("telemetry")
    suspend fun sendTelemetry(
        @Header("Authorization") token: String,
        @Body request: TelemetryRequest
    ): Response<TelemetryResponse>
}

data class TelemetryRequest(
    val action: String = "PULSE",
    val user_id: String,
    val status: String,
    val latency_ms: Long,
    val app_version: String,
    val device_info: Map<String, String>
)

data class TelemetryResponse(
    val success: Boolean,
    val server_time: String? = null
)

data class RegisterRequest(
    val action: String = "register",
    val intent: String = "REGISTER",
    val email: String,
    val password: String,
    val phone_number: String = ""
)

data class VerifyRequest(
    val action: String = "verify",
    val phone_number: String,
    val code: String,
    val password: String? = null
)

data class ResendRequest(
    val action: String = "resend",
    val phone_number: String
)

data class RefreshRequest(
    val action: String = "refresh",
    val refresh_token: String,
    val email: String
)

data class LogoutRequest(
    val action: String = "logout",
    val user_id: String,
    val access_token: String? = null
)

data class OtpFetchRequest(
    val action: String = "fetch_test_otp",
    val phone_number: String
)

data class OtpFetchResponse(
    val success: Boolean,
    val otp: String?,
    val timestamp: Long?,
    val message: String?
)

data class DeviceTokenRequest(
    val action: String = "UPDATE_FCM",
    val device_token: String,
    val device_type: String = "ANDROID"
)

data class LoginRequest(
    val action: String = "login",
    val intent: String = "LOGIN",
    val email: String,
    val password: String,
    val phone_number: String = ""
)

data class AuthResponse(
    val success: Boolean,
    val tokens: TokenData?,
    val message: String?,
    val error: String? = null,
    val user_id: String? = null
)

data class TokenData(
    val access_token: String,
    val id_token: String,
    val refresh_token: String
)

data class MarketDataResponse(
    val results: List<MarketData>,
    val lead_symbol: String? = null,
    val lead_signal: String? = null,
    val lead_payload: String? = null
)

data class MarketData(
    val symbol: String,
    val price: Double,
    val volume_24h: Double,
    val price_change_24h: Double,
    val kline_data: List<List<Any>>? = null,
    val min_trade_amount: Double? = 1.0,
    val entry_timeframe: String? = null,
    val best_timeframe: String? = null,
    val min_qty: Double? = 0.0,
    val step_size: Double? = 0.0,
    val tick_size: Double? = 0.0,
    val min_notional: Double? = 10.0,
    val status: String? = "Trading",
    val rvol: Double? = null,
    val spread_pct: Double? = null,
    val volatility: Double? = null,
    val reason: String? = null,
    val signal_tag: String? = null,
    val matrix: Map<String, Any>? = null,
    val analysis_details: Map<String, Any>? = null,
    val signal: String? = null,
    val confidence: Int? = null,
    @com.google.gson.annotations.SerializedName("entryPrice") val entryPrice: Double? = null,
    @com.google.gson.annotations.SerializedName("stopLoss") val stopLoss: Double? = null,
    @com.google.gson.annotations.SerializedName("takeProfit") val takeProfit: Double? = null,
    val strategy: String? = null,
    val verification: VerificationStatus? = null,
    val rsi: Double? = null,
    val adx: Double? = null,
    val timeframe: String? = null
)

data class TradeSignalsResponse(
    val signals: List<TradeSignal>
)

data class TradeSignal(
    val strategy: String,
    val signal: String,
    val confidence: Int,
    val entry_price: Double,
    val stop_loss: Double,
    val take_profit: Double,
    val reason: String
)

data class TradeRequest(
    val user_id: String,
    val symbol: String,
    val side: String,
    val quantity: Double,
    val investment_usdt: Double? = null,
    val price: Double,
    val api_key: String? = null,
    val api_secret: String? = null,
    val exchange: String? = null,
    val detected_entry: Double? = null,
    val detected_sl: Double? = null,
    val detected_tp: Double? = null,
    val action: String? = null,
    val env: String,
    val signal_id: String? = null,
    val trade_id: String? = null,
    val sl_order_id: String? = null,
    val tp_order_id: String? = null,
    val reduce_only: Boolean? = null
)

data class TradeResponse(
    val success: Boolean,
    val trade_id: String?,
    val message: String?,
    val environment: String? = null,
    val account_type: String? = null,
    val trade: TradeData? = null,
    val constraints: ExchangeConstraints? = null,
    val balances: List<AssetBalance>? = null,
    @com.google.gson.annotations.SerializedName("total_equity") val total_equity: Double? = null,
    @com.google.gson.annotations.SerializedName("qty") val quantity: Double? = null,
    @com.google.gson.annotations.SerializedName("entry_price") val entryPrice: Double? = null,
    @com.google.gson.annotations.SerializedName("sl_order") val stopLossOrderId: String? = null,
    @com.google.gson.annotations.SerializedName("tp_order") val takeProfitOrderId: String? = null,
    val symbol: String? = null,
    val side: String? = null
)

data class AssetBalance(
    val asset: String,
    val free: Double
)

data class ActiveTradesResponse(
    val trades: List<ActiveTradeData>
)

data class ActiveTradeData(
    val trade_id: String,
    val symbol: String,
    val side: String,
    val entry_price: Double,
    val qty: Double,
    val signal_id: String? = null,
    val sl_price: Double?,
    val tp_price: Double?,
    val unrealized_pnl: Double? = null,
    val last_price: Double? = null,
    val status: String,
    val exchange: String,
    val env: String,
    val created_at: String,
    val exit_price: Double? = null,
    val realized_pnl: Double? = null,
    val exit_reason: String? = null,
    val closed_at: String? = null
)

data class ExchangeConstraints(
    val min_notional: Double,
    val step_size: Double,
    val reference_pair: String
)

data class TradeData(
    val order_id: String?,
    val symbol: String?,
    val side: String?,
    val executed_price: Double?,
    val executed_sl: Double? = null,
    val executed_tp: Double? = null,
    val quantity: Double?,
    val status: String?,
    val exchange: String?,
    val environment: String?
)

data class SubscriptionResponse(
    val user_id: String?,
    val subscription: SubscriptionDetails?
)

data class SubscriptionDetails(
    val status: String,
    val subscription_type: String,
    val is_active: Boolean,
    @com.google.gson.annotations.SerializedName("expiry_date") val expiryDate: String?,
    @com.google.gson.annotations.SerializedName("start_date") val startDate: String?,
    @com.google.gson.annotations.SerializedName("days_remaining") val daysRemaining: Int = 0,
    val features: Map<String, Any>? = null
)

data class VerifyPurchaseRequest(
    val action: String = "verify_purchase",
    val user_id: String,
    val productId: String,
    val token: String
)

data class VerifyPurchaseResponse(
    val success: Boolean,
    val message: String,
    val expiry: Long? = null
)

data class ValidationRequest(
    val action: String = "VALIDATE",
    val api_key: String,
    val api_secret: String,
    val exchange: String,
    val env: String
)

data class TradeListRequest(
    val user_id: String,
    val exchange: String? = null,
    val env: String? = null,
    val api_key: String? = null,
    val api_secret: String? = null,
    val action: String = "LIST_OPEN"
)

data class ScanMarketResponse(
    val statusCode: Int? = null,
    val body: String? = null,
    val status: String? = null,
    val signals_generated: Int? = null,
    val details: List<StrategyEvaluationResponse>? = null
)

data class StrategyEvaluationRequest(
    val symbol: String? = null,
    val strategy: String? = null,
    val user_id: String? = null,
    val exchange: String? = null,
    val timeframe: String? = null,
    val risk_profile: String? = null,
    val env: String,
    val api_key: String? = null,
    val api_secret: String? = null,
    val action: String? = null,
    val parameters: Map<String, Double>? = null
)

data class StrategyEvaluationResponse(
    val signal: String,
    val confidence: Int,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val reason: String,
    val timestamp: String,
    val strategy: String? = null,
    val verification: VerificationStatus? = null,
    val symbol: String? = null
)

data class VerificationStatus(
    val status: String,
    val reason: String?
)
