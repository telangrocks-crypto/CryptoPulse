package com.cryptopulse.trader.data.repository

import com.cryptopulse.trader.data.api.CryptoPulseApi
import com.cryptopulse.trader.data.api.AuthResponse
import com.cryptopulse.trader.data.api.LoginRequest
import com.cryptopulse.trader.data.api.RegisterRequest
import com.cryptopulse.trader.data.api.VerifyRequest
import com.cryptopulse.trader.data.api.RefreshRequest
import com.cryptopulse.trader.data.api.LogoutRequest
import com.cryptopulse.trader.data.local.UserPreferences
import com.cryptopulse.trader.data.service.CognitoAuthService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: CryptoPulseApi,
    private val cognitoAuthService: CognitoAuthService,
    private val userPreferences: UserPreferences
) : AuthRepository {

    override suspend fun login(email: String, pass: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            // Use dummy phone number for backend compatibility while focusing on email UX
            val response = api.login(LoginRequest(email = email, password = pass, phone_number = "+919876543210"))
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!
                auth.tokens?.let {
                    userPreferences.accessToken = it.access_token
                    userPreferences.refreshToken = it.refresh_token
                    userPreferences.authToken = it.id_token
                    userPreferences.userId = auth.user_id
                    userPreferences.userEmail = email
                }
                Result.success(auth)
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, pass: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            // Use dummy phone number for backend compatibility while focusing on email UX
            val response = api.register(RegisterRequest(email = email, password = pass, phone_number = "+919876543210"))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Registration failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verify(email: String, code: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.verify(VerifyRequest(phone_number = email, code = code))
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!
                auth.tokens?.let {
                    userPreferences.accessToken = it.access_token
                    userPreferences.refreshToken = it.refresh_token
                    userPreferences.authToken = it.id_token
                    userPreferences.userId = auth.user_id
                    userPreferences.userEmail = email
                }
                Result.success(auth)
            } else {
                Result.failure(Exception("Verification failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshToken(): Boolean = withContext(Dispatchers.IO) {
        try {
            val refresh = userPreferences.refreshToken ?: return@withContext false
            val email = userPreferences.userEmail ?: return@withContext false
            
            val response = api.refreshToken(RefreshRequest(refresh_token = refresh, email = email))
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!
                auth.tokens?.let {
                    userPreferences.accessToken = it.access_token
                    userPreferences.refreshToken = it.refresh_token
                    userPreferences.authToken = it.id_token
                    return@withContext true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun logout(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val userId = userPreferences.userId ?: return@withContext Result.success(true)
            api.logout(LogoutRequest(user_id = userId, access_token = userPreferences.accessToken))
            
            userPreferences.authToken = null
            userPreferences.accessToken = null
            userPreferences.refreshToken = null
            userPreferences.userId = null
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return userPreferences.authToken != null
    }
}
