package com.cryptopulse.trader.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cryptopulse.trader.data.api.MarketData
import com.cryptopulse.trader.data.local.UserPreferences
import com.cryptopulse.trader.data.repository.AuthRepository
import com.cryptopulse.trader.data.repository.SubscriptionRepository
import com.cryptopulse.trader.databinding.ActivityDashboardBinding
import com.cryptopulse.trader.ui.auth.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var binding: ActivityDashboardBinding

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var subscriptionRepository: SubscriptionRepository
    @Inject lateinit var userPreferences: UserPreferences

    private val marketAdapter = MarketDataAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()
        setupClickListeners()

        // Initial data load
        viewModel.fetchData()
    }

    private fun setupRecyclerView() {
        binding.rvMarketData.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = marketAdapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                binding.loaderOverlay.root.visibility = if (loading) View.VISIBLE else View.GONE
                binding.swipeRefresh.isRefreshing = loading
            }
        }

        lifecycleScope.launch {
            viewModel.marketData.collect { data ->
                if (data.isEmpty()) {
                    binding.tvNoData.visibility = View.VISIBLE
                    binding.rvMarketData.visibility = View.GONE
                } else {
                    binding.tvNoData.visibility = View.GONE
                    binding.rvMarketData.visibility = View.VISIBLE
                    marketAdapter.submitList(data)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.activeTrades.collect { trades ->
                // Active positions section — update badge count
                // Full implementation in active positions include layout
            }
        }

        lifecycleScope.launch {
            viewModel.subscriptionStatus.collect { sub ->
                val isActive = sub?.subscription?.is_active == true
                binding.subscriptionLockOverlayGroup.root.visibility =
                    if (isActive) View.GONE else View.VISIBLE
            }
        }

        lifecycleScope.launch {
            viewModel.exchangeBalance.collect { balance ->
                if (balance != null && balance > 0.0) {
                    binding.layoutExchangeStatus.visibility = View.VISIBLE
                    binding.tvExchangeBalance.text = String.format("%.2f", balance)
                    binding.btnConnectAPI.visibility = View.GONE
                    binding.btnChangeApiKey.visibility = View.VISIBLE
                } else {
                    binding.layoutExchangeStatus.visibility = View.GONE
                    binding.btnConnectAPI.visibility = View.VISIBLE
                    binding.btnChangeApiKey.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is DashboardViewModel.DashboardEvent.ShowError -> {
                        Toast.makeText(this@DashboardActivity, event.message, Toast.LENGTH_LONG).show()
                    }
                    is DashboardViewModel.DashboardEvent.OpenSetEntry -> {
                        Toast.makeText(this@DashboardActivity, "Opening Entry Settings...", Toast.LENGTH_SHORT).show()
                    }
                    is DashboardViewModel.DashboardEvent.OpenSimulateTrade -> {
                        Toast.makeText(this@DashboardActivity, "Opening Simulation Engine...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        // Pull-to-refresh
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchData()
        }

        // API Key setup
        binding.btnConnectAPI.setOnClickListener {
            startActivity(Intent(this, Class.forName("com.cryptopulse.trader.ui.apikey.ApiKeyActivity")))
        }
        binding.btnChangeApiKey.setOnClickListener {
            startActivity(Intent(this, Class.forName("com.cryptopulse.trader.ui.apikey.ApiKeyActivity")))
        }

        // Set Entry / Position Size
        binding.btnSetEntryPrice.setOnClickListener {
            viewModel.onSetEntryTapped()
        }

        // Scan Markets
        binding.btnFetchCoins.setOnClickListener {
            binding.cardMarketData.visibility =
                if (binding.cardMarketData.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // Strategy Results Matrix
        binding.btnScannerMatrix.setOnClickListener {
            val v = binding.layoutScannerMatrix.root
            v.visibility = if (v.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // Simulate / Execute Trade
        binding.btnExecuteTrade.setOnClickListener {
            viewModel.onSimulateTrade()
        }

        // Active Positions toggle
        binding.btnViewActivePositions.setOnClickListener {
            val v = binding.activePositionsSection.root
            v.visibility = if (v.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // Account management
        binding.btnSubscribe.setOnClickListener {
            startActivity(Intent(this, Class.forName("com.cryptopulse.trader.ui.subscription.SubscriptionActivity")))
        }
        binding.btnChangeMobile.setOnClickListener {
            startActivity(Intent(this, Class.forName("com.cryptopulse.trader.ui.auth.ChangeMobileActivity")))
        }
        binding.btnLogout.setOnClickListener {
            performLogout()
        }
        binding.btnExportLogs.setOnClickListener {
            Toast.makeText(this, "Diagnostics export initiated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogout() {
        lifecycleScope.launch {
            authRepository.logout()
            val intent = Intent(this@DashboardActivity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}
