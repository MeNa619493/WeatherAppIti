package com.example.weatherapp.ui.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemHourlyBinding
import com.example.weatherapp.model.pojo.Hourly

class HourlyAdapter : ListAdapter<Hourly, HourlyAdapter.MyViewHolder>(
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