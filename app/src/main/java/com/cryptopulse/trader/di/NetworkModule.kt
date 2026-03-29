package com.cryptopulse.trader.di

import com.cryptopulse.trader.data.api.CryptoPulseApi
import com.cryptopulse.trader.data.repository.AuthRepository
import com.cryptopulse.trader.data.repository.AuthRepositoryImpl
import com.cryptopulse.trader.data.local.UserPreferences
import com.cryptopulse.trader.data.service.CognitoAuthService
import com.cryptopulse.trader.util.DiagnosticsLogger
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        diagnosticsLogger: com.cryptopulse.trader.util.DiagnosticsLogger,
        userPreferences: com.cryptopulse.trader.data.local.UserPreferences,
        authRepositoryProvider: javax.inject.Provider<com.cryptopulse.trader.data.repository.AuthRepository>
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        
        logging.level = if (com.cryptopulse.trader.BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.HEADERS 
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        
        return OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                var originalRequest = chain.request()
                val path = originalRequest.url.encodedPath
                val method = originalRequest.method
                
                fun Request.withBearer(token: String?): Request {
                    if (token == null) return this
                    val headerValue = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
                    return this.newBuilder()
                        .header("Authorization", headerValue)
                        .build()
                }

                var builder = originalRequest.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "CryptoPulse-Android/1.0")
                
                var request = builder.build()

                if (originalRequest.header("Authorization") == null && !path.contains("auth")) {
                    val currentToken = userPreferences.authToken
                    if (currentToken != null) {
                        request = request.withBearer(currentToken)
                    }
                }

                val startNs = System.nanoTime()
                var response: Response
                try {
                    response = chain.proceed(request)
                } catch (e: Exception) {
                    diagnosticsLogger.log("NETWORK", "FAIL: $method $path -> ${e.message}", com.cryptopulse.trader.util.DiagnosticsLogger.LogLevel.ERROR)
                    throw e
                }

                if (response.code == 401 && !path.contains("auth")) {
                    synchronized(this) {
                        val tokenBeforeWaiting = request.header("Authorization")
                        val currentTokenInPrefs = userPreferences.authToken
                        
                        if (tokenBeforeWaiting != null && !tokenBeforeWaiting.contains(currentTokenInPrefs ?: "")) {
                            response.close()
                            val newRequest = request.withBearer(currentTokenInPrefs)
                            response = chain.proceed(newRequest)
                        } else {
                            val refreshed = kotlinx.coroutines.runBlocking {
                                try {
                                    authRepositoryProvider.get().refreshToken()
                                } catch (e: Exception) {
                                    false
                                }
                            }

                            if (refreshed) {
                                val newToken = userPreferences.authToken
                                response.close()
                                val newRequest = request.withBearer(newToken)
                                response = chain.proceed(newRequest)
                            } else {
                                if (userPreferences.authToken != null) {
                                    userPreferences.authToken = null
                                    userPreferences.userId = null
                                    
                                    val intent = android.content.Intent(diagnosticsLogger.context, com.cryptopulse.trader.ui.auth.LoginActivity::class.java).apply {
                                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                    diagnosticsLogger.context.startActivity(intent)
                                }
                            }
                        }
                    }
                }

                val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
                
                if (response.isSuccessful) {
                    if (path.contains("auth")) {
                         diagnosticsLogger.log("API_SUCCESS", "$method /auth synchronized (${tookMs}ms)")
                    }
                } else {
                    val code = response.code
                    if (code != 401) {
                        diagnosticsLogger.log("API_ERROR", "HTTP $code: $method $path", com.cryptopulse.trader.util.DiagnosticsLogger.LogLevel.WARNING)
                    }
                }
                
                response
            }
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            // ✅ PRODUCTION API ENPOINT FROM PIPELINE
            .baseUrl("https://hs28uxr9j6.execute-api.ap-south-1.amazonaws.com/prod/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideCryptoPulseApi(retrofit: Retrofit): CryptoPulseApi {
        return retrofit.create(CryptoPulseApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        cryptoPulseApi: CryptoPulseApi,
        cognitoAuthService: CognitoAuthService,
        userPreferences: UserPreferences
    ): AuthRepository {
        return AuthRepositoryImpl(cryptoPulseApi, cognitoAuthService, userPreferences)
    }
    
    @Provides
    @Singleton
    fun provideCognitoAuthService(
        @ApplicationContext context: Context,
        userPreferences: UserPreferences,
        diagnosticsLogger: DiagnosticsLogger
    ): CognitoAuthService {
        return CognitoAuthService(context, userPreferences, diagnosticsLogger)
    }
}
