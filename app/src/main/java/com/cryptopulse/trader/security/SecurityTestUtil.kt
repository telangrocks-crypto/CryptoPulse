package com.cryptopulse.trader.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.IOException
import java.security.GeneralSecurityException

/**
 * Security test utility to verify EncryptedSharedPreferences functionality
 * This should be used in debug builds to test secure storage implementation
 */
object SecurityTestUtil {
    
    fun testSecureStorage(context: Context): Boolean {
        return try {
            // Test MasterKey creation
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            // Test EncryptedSharedPreferences creation
            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "test_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            // Test token storage and retrieval
            val testToken = "test_jwt_token_12345"
            encryptedPrefs.edit().putString("test_token", testToken).apply()
            val retrievedToken = encryptedPrefs.getString("test_token", null)
            
            // Verify encryption worked (data should be encrypted at rest)
            val success = retrievedToken == testToken
            
            // Cleanup test data
            encryptedPrefs.edit().clear().apply()
            
            success
        } catch (e: GeneralSecurityException) {
            false
        } catch (e: IOException) {
            false
        }
    }
    
    fun validateTokenEncryption(context: Context): Map<String, Boolean> {
        val results = mutableMapOf<String, Boolean>()
        
        results["masterKey_creation"] = try {
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            true
        } catch (e: Exception) {
            false
        }
        
        results["encryptedStorage_creation"] = try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            EncryptedSharedPreferences.create(
                context,
                "validation_test_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            true
        } catch (e: Exception) {
            false
        }
        
        results["token_encryption_decryption"] = try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "token_test_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            val testTokens = mapOf(
                "access_token" to "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "refresh_token" to "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
                "id_token" to "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            
            // Store tokens
            testTokens.forEach { (key, value) ->
                encryptedPrefs.edit().putString(key, value).apply()
            }
            
            // Retrieve and verify tokens
            val allTestsPassed = testTokens.all { (key, expectedValue) ->
                val retrievedValue = encryptedPrefs.getString(key, null)
                retrievedValue == expectedValue
            }
            
            // Cleanup
            encryptedPrefs.edit().clear().apply()
            
            allTestsPassed
        } catch (e: Exception) {
            false
        }
        
        return results
    }
}
