package com.cryptopulse.trader.ui.apikey

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cryptopulse.trader.databinding.ActivityApiKeyBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ApiKeyActivity : AppCompatActivity() {

    private val viewModel: ApiKeyViewModel by viewModels()
    private lateinit var binding: ActivityApiKeyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApiKeyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        // Setup Exchange Spinner (Bybit only as per production spec)
        val exchanges = arrayOf("Bybit")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, exchanges)
        binding.spinnerExchange.setAdapter(adapter)
        binding.spinnerExchange.setText(exchanges[0], false)

        binding.btnValidateAndSave.setOnClickListener {
            val exchange = binding.spinnerExchange.text.toString().lowercase()
            val apiKey = binding.etApiKey.text.toString().trim()
            val apiSecret = binding.etApiSecret.text.toString().trim()
            val env = if (binding.toggleEnv.checkedButtonId == binding.btnMainnet.id) "prod" else "testnet"

            if (apiKey.isEmpty() || apiSecret.isEmpty()) {
                Toast.makeText(this, "API Key and Secret required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.validateAndSave(exchange, apiKey, apiSecret, env)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { loading ->
                binding.btnValidateAndSave.isEnabled = !loading
                if (loading) {
                    binding.tvValidationStatus.apply {
                        visibility = View.VISIBLE
                        text = "Validating with exchange..."
                        setTextColor(android.graphics.Color.GRAY)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.validationResult.collectLatest { result ->
                result.onSuccess {
                    binding.tvValidationStatus.apply {
                        text = "Success! Keys validated and saved."
                        setTextColor(android.graphics.Color.parseColor("#00C87E"))
                    }
                    Toast.makeText(this@ApiKeyActivity, "Exchange Connected Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }.onFailure {
                    binding.tvValidationStatus.apply {
                        text = "Validation Failed: ${it.message}"
                        setTextColor(android.graphics.Color.RED)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.savedConfig.collectLatest { config ->
                config?.let {
                    binding.etApiKey.setText(it.apiKey)
                    binding.etApiSecret.setText(it.apiSecret)
                    if (it.env == "prod") {
                        binding.toggleEnv.check(binding.btnMainnet.id)
                    } else {
                        binding.toggleEnv.check(binding.btnTestnet.id)
                    }
                }
            }
        }
    }
}
