package com.cryptopulse.trader.data.repository

import com.cryptopulse.trader.data.api.AuthResponse

interface AuthRepository {
    suspend fun login(email: String, pass: String): Result<AuthResponse>
    suspend fun register(email: String, pass: String): Result<AuthResponse>
    suspend fun verify(email: String, code: String): Result<AuthResponse>
    suspend fun refreshToken(): Boolean
    suspend fun logout(): Result<Boolean>
    fun isUserLoggedIn(): Boolean
}
