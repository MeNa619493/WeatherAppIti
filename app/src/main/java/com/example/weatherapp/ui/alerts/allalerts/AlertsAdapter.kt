package com.example.weatherapp.ui.alerts.allalerts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemAlarmBinding
import com.example.weatherapp.model.pojo.WeatherAlert
import com.example.weatherapp.utils.Constants
import com.example.weatherapp.utils.Constants.convertLongToDayDateAlert

class AlertsAdapter() : ListAdapter<WeatherAlert, AlertsAdapter.MyViewHolder>(
    AlertsDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class MyViewHolder(private val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(weatherAlert: WeatherAlert) {
            binding.apply {
                dateFrom.text = convertLongToDayDateAlert(weatherAlert.startDate)
                dateTo.text = convertLongToDayDateAlert(weatherAlert.endDate)
                hourFrom.text = Constants.convertLongToTimePicker(weatherAlert.timeFrom)
                hourTo.text = Constants.convertLongToTimePicker(weatherAlert.timeTo)
            }
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemAlarmBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    class AlertsDiffCallback : DiffUtil.ItemCallback<WeatherAlert>() {
        override fun areItemsTheSame(oldItem: WeatherAlert, newItem: WeatherAlert): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WeatherAlert, newItem: WeatherAlert): Boolean {
            return oldItem == newItem
        }
    }
}