package com.dev.weatherapp.ui.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev.weatherapp.R
import com.dev.weatherapp.databinding.ItemHourlyBinding
import com.dev.weatherapp.model.pojo.Hourly
import com.dev.weatherapp.utils.Utils
import com.dev.weatherapp.utils.Utils.getTemperatureUnit

class HourlyAdapter :
    ListAdapter<Hourly, HourlyAdapter.MyViewHolder>(
        HourlyDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MyViewHolder(private val binding: ItemHourlyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(hourly: Hourly) {
            binding.apply {
                tvHour.text = hourly.dt?.let { Utils.convertLongToTime(it) }
                val strFormat: String = binding.root.context.getString(
                    R.string.hourly,
                    hourly.temp?.toInt(),
                    getTemperatureUnit(binding.root.context)
                )
                tvTemp.text = strFormat

                Glide
                    .with(binding.root)
                    .load("https://openweathermap.org/img/wn/${hourly.weather?.get(0)?.icon ?: ""}.png?fbclid=IwAR2Nk0UQ5anrxUCLubc6bRZTqN65qD2TE2Rk0EvU6-609jRf_HuHPAnP-YE")
                    .into(ivHourDesc)
            }
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemHourlyBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }

    }

    class HourlyDiffCallback : DiffUtil.ItemCallback<Hourly>() {
        override fun areItemsTheSame(oldItem: Hourly, newItem: Hourly): Boolean {
            return oldItem.dt == newItem.dt
        }

        override fun areContentsTheSame(oldItem: Hourly, newItem: Hourly): Boolean {
            return oldItem == newItem
        }
    }
}