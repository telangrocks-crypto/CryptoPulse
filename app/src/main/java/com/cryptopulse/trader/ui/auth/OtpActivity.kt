package com.cryptopulse.trader.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cryptopulse.trader.data.repository.AuthRepository
import com.cryptopulse.trader.databinding.ActivityOtpBinding
import com.cryptopulse.trader.ui.dashboard.DashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OtpActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    private lateinit var binding: ActivityOtpBinding

    private var phoneNumber: String = ""
    private var userEmail: String = ""
    private var countDownTimer: CountDownTimer? = null
    private val OTP_RESEND_DELAY_MS = 30_000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        phoneNumber = intent.getStringExtra("phone_number") ?: ""
        userEmail = intent.getStringExtra("user_email") ?: ""
        binding.tvDescription.text = "Please enter the verification code sent to\n$userEmail"

        setupListeners()
        startResendTimer()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnVerify.setOnClickListener {
            val otp = binding.etOtp.text.toString().trim()
            if (otp.length != 6) {
                Toast.makeText(this, "Please enter the 6-digit OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyOtp(otp)
        }

        binding.tvResend.setOnClickListener {
            resendOtp()
        }
    }

    private fun verifyOtp(otp: String) {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnVerify.isEnabled = false

            val result = authRepository.verify(userEmail, otp) // Use email for backend

            binding.progressBar.visibility = View.GONE
            binding.btnVerify.isEnabled = true

            result.onSuccess {
                Toast.makeText(this@OtpActivity, "Account verified! Welcome to CryptoPulse.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@OtpActivity, DashboardActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }.onFailure {
                Toast.makeText(this@OtpActivity, it.message ?: "Verification failed. Please try again.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun resendOtp() {
        lifecycleScope.launch {
            binding.tvResend.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            val result = authRepository.register(userEmail, "dummy_password") // Use email for backend

            binding.progressBar.visibility = View.GONE
            result.onSuccess {
                Toast.makeText(this@OtpActivity, "OTP resent to $userEmail", Toast.LENGTH_SHORT).show()
                startResendTimer()
            }.onFailure {
                Toast.makeText(this@OtpActivity, "Failed to resend OTP: ${it.message}", Toast.LENGTH_LONG).show()
                binding.tvResend.isEnabled = true
            }
        }
    }

    private fun startResendTimer() {
        binding.tvResend.isEnabled = false
        binding.tvTimer.visibility = View.VISIBLE

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(OTP_RESEND_DELAY_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvTimer.text = "Resend in 00:%02d".format(seconds)
            }

            override fun onFinish() {
                binding.tvTimer.visibility = View.GONE
                binding.tvResend.isEnabled = true
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
