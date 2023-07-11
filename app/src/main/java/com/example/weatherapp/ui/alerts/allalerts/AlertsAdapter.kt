package com.example.weatherapp.ui.alerts.allalerts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemAlarmBinding
import com.example.weatherapp.model.pojo.WeatherAlert
import com.example.weatherapp.utils.Utils
import com.example.weatherapp.utils.Utils.convertLongToDayDateAlert

class AlertsAdapter(
    private val clickListener: AlertClickListener
) : ListAdapter<WeatherAlert, AlertsAdapter.MyViewHolder>(
    AlertsDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    interface AlertClickListener {
        fun onDeleteItemClicked(weatherAlert: WeatherAlert)
    }

    class MyViewHolder(private val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(weatherAlert: WeatherAlert, clickListener: AlertClickListener) {
            binding.apply {
                dateFrom.text = convertLongToDayDateAlert(weatherAlert.startDate)
                dateTo.text = convertLongToDayDateAlert(weatherAlert.endDate)
                hourFrom.text = Utils.convertLongToTimePicker(weatherAlert.timeFrom)
                hourTo.text = Utils.convertLongToTimePicker(weatherAlert.timeTo)

                ivDelete.setOnClickListener {
                    clickListener.onDeleteItemClicked(weatherAlert)
                }
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