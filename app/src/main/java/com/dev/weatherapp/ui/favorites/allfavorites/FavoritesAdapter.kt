package com.dev.weatherapp.ui.favorites.allfavorites

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dev.weatherapp.databinding.FavItemBinding
import com.dev.weatherapp.model.pojo.WeatherResponse
import com.dev.weatherapp.utils.Utils

class FavoritesAdapter(
    private val context: Context,
    private val clickListener: WeatherResponseClickListener
) : ListAdapter<WeatherResponse, FavoritesAdapter.MyViewHolder>(
    WeatherResponseDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, context)
    }

    interface WeatherResponseClickListener {
        fun onItemClicked(weatherResponse: WeatherResponse)
        fun onDeleteItemClicked(weatherResponse: WeatherResponse)
    }

    class MyViewHolder(private val binding: FavItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(weatherResponse: WeatherResponse, clickListener: WeatherResponseClickListener, context: Context) {
            binding.apply {
                tvLocation.text = Utils.getAddress(context, weatherResponse.lat ?:0.0, weatherResponse.lon ?:0.0)

                layout.setOnClickListener {
                    clickListener.onItemClicked(weatherResponse)
                }

                ivDelete.setOnClickListener {
                    clickListener.onDeleteItemClicked(weatherResponse)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FavItemBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    class WeatherResponseDiffCallback : DiffUtil.ItemCallback<WeatherResponse>() {
        override fun areItemsTheSame(oldItem: WeatherResponse, newItem: WeatherResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: WeatherResponse,
            newItem: WeatherResponse
        ): Boolean {
            return oldItem == newItem
        }
    }
}