package com.cryptopulse.trader.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cryptopulse.trader.data.repository.AuthRepository
import com.cryptopulse.trader.databinding.ActivityMainBinding
import com.cryptopulse.trader.ui.auth.LoginActivity
import com.cryptopulse.trader.ui.dashboard.DashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository
    
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (authRepository.isUserLoggedIn()) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
