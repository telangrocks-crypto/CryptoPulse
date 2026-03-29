package com.cryptopulse.trader.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cryptopulse.trader.data.api.MarketData
import com.cryptopulse.trader.databinding.ItemMarketDataBinding

class MarketDataAdapter(
    private val onItemClick: ((MarketData) -> Unit)? = null
) : ListAdapter<MarketData, MarketDataAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MarketData>() {
            override fun areItemsTheSame(old: MarketData, new: MarketData) = old.symbol == new.symbol
            override fun areContentsTheSame(old: MarketData, new: MarketData) = old == new
        }
    }

    inner class ViewHolder(private val binding: ItemMarketDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MarketData) {
            binding.tvSymbol.text = item.symbol
            binding.tvPrice.text = String.format("$%.4f", item.price)

            val change = item.price_change_24h
            val changeText = String.format("%+.2f%%", change)
            binding.tvChange.text = changeText
            binding.tvChange.setTextColor(
                if (change >= 0) Color.parseColor("#00C87E") else Color.parseColor("#FF4C4C")
            )

            // Signal badge (live from backend AI strategy)
            val signal = item.signal
            if (!signal.isNullOrBlank()) {
                binding.tvSignal.text = signal
                binding.tvSignal.visibility = android.view.View.VISIBLE
                binding.tvSignal.setTextColor(
                    when (signal.uppercase()) {
                        "BUY" -> Color.parseColor("#00C87E")
                        "SELL" -> Color.parseColor("#FF4C4C")
                        else -> Color.parseColor("#FFD700")
                    }
                )
            } else {
                binding.tvSignal.visibility = android.view.View.GONE
            }

            binding.root.setOnClickListener { onItemClick?.invoke(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMarketDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
