package com.cryptopulse.trader.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cryptopulse.trader.data.repository.AuthRepository
import com.cryptopulse.trader.databinding.ActivityLoginBinding
import com.cryptopulse.trader.ui.dashboard.DashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass  = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter your email address and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (!isValidEmail(email)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            performLogin(email, pass)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }

    private fun performLogin(email: String, pass: String) {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false

            val result = authRepository.login(email, pass)

            binding.progressBar.visibility = View.GONE
            binding.btnLogin.isEnabled = true

            result.onSuccess {
                val intent = Intent(this@LoginActivity, DashboardActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }.onFailure {
                Toast.makeText(this@LoginActivity, it.message ?: "Login failed. Please try again.", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
