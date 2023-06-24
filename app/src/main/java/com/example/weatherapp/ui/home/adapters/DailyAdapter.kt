package com.example.weatherapp.ui.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemDailyBinding
import com.example.weatherapp.model.pojo.Daily

class DailyAdapter : ListAdapter<Daily, DailyAdapter.MyViewHolder>(
    DailyDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MyViewHolder(private val binding: ItemDailyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(daily: Daily) {

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