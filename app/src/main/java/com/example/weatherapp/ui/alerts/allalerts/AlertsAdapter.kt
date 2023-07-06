package com.example.weatherapp.ui.alerts.allalerts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemAlarmBinding
import com.example.weatherapp.model.pojo.WeatherAlert
import com.example.weatherapp.model.pojo.WeatherResponse
import com.example.weatherapp.ui.favorites.allfavorites.FavoritesAdapter
import com.example.weatherapp.utils.Constants
import com.example.weatherapp.utils.Constants.convertLongToDayDateAlert

class AlertsAdapter(
    private val language: String,
    private val clickListener: AlertClickListener
) : ListAdapter<WeatherAlert, AlertsAdapter.MyViewHolder>(
    AlertsDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, language)
    }

    interface AlertClickListener {
        fun onDeleteItemClicked(weatherAlert: WeatherAlert)
    }

    class MyViewHolder(private val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(weatherAlert: WeatherAlert, clickListener: AlertClickListener, language: String) {
            binding.apply {
                dateFrom.text = convertLongToDayDateAlert(weatherAlert.startDate, language)
                dateTo.text = convertLongToDayDateAlert(weatherAlert.endDate, language)
                hourFrom.text = Constants.convertLongToTimePicker(weatherAlert.timeFrom, language)
                hourTo.text = Constants.convertLongToTimePicker(weatherAlert.timeTo, language)

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