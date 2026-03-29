package com.cryptopulse.trader.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cryptopulse.trader.data.repository.AuthRepository
import com.cryptopulse.trader.databinding.ActivityRegistrationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RegistrationActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    private lateinit var binding: ActivityRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.length < 8) {
                Toast.makeText(this, "Valid email and 8+ char password required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (!isValidEmail(email)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performRegistration(email, password)
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun performRegistration(email: String, pass: String) {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnRegister.isEnabled = false
            
            val result = authRepository.register(email, pass)
            
            binding.progressBar.visibility = View.GONE
            binding.btnRegister.isEnabled = true

            result.onSuccess {
                Toast.makeText(this@RegistrationActivity, "Registration successful! Please check your email for verification.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@RegistrationActivity, OtpActivity::class.java).apply {
                    putExtra("phone_number", "+919876543210") // Dummy phone for backend compatibility
                    putExtra("user_email", email) // Pass actual email for display
                }
                startActivity(intent)
                finish()
            }.onFailure {
                Toast.makeText(this@RegistrationActivity, it.message ?: "Registration failed", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
