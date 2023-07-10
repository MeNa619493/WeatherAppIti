package com.example.weatherapp.ui.home.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ItemDailyBinding
import com.example.weatherapp.model.local.HelperSharedPreferences
import com.example.weatherapp.model.pojo.Daily
import com.example.weatherapp.utils.Constants
import com.example.weatherapp.utils.Constants.getTemperatureUnit

class DailyAdapter(private val language: String) :
    ListAdapter<Daily, DailyAdapter.MyViewHolder>(
        DailyDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position), language)
    }

    class MyViewHolder(private val binding: ItemDailyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(daily: Daily, language: String) {
            binding.apply {
                daily.dt?.let {
                    tvDay.text = Constants.convertLongToDayName(it, language)
                }
                tvDayStatus.text = daily.weather?.get(0)?.description ?: ""

                val strFormat: String = binding.root.context.getString(
                    R.string.daily,
                    daily.temp?.max?.toInt(),
                    daily.temp?.min?.toInt(),
                    getTemperatureUnit(binding.root.context)
                )
                tvMaxMinTemp.text = strFormat

                Glide
                    .with(binding.root)
                    .load("https://openweathermap.org/img/wn/${daily.weather?.get(0)?.icon ?: ""}.png?fbclid=IwAR2Nk0UQ5anrxUCLubc6bRZTqN65qD2TE2Rk0EvU6-609jRf_HuHPAnP-YE")
                    .into(ivDayDesc)
            }
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemDailyBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }

    }

    class DailyDiffCallback : DiffUtil.ItemCallback<Daily>() {
        override fun areItemsTheSame(oldItem: Daily, newItem: Daily): Boolean {
            return oldItem.dt == newItem.dt
        }

        override fun areContentsTheSame(oldItem: Daily, newItem: Daily): Boolean {
            return oldItem == newItem
        }
    }
}