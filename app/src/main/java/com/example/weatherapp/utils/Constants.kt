package com.example.weatherapp.utils

import android.util.Log
import com.example.weatherapp.ui.home.HomeFragment
import java.text.SimpleDateFormat
import java.util.*

object Constants {
    const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    const val API_KEY = "cad8edbd7bf4c844f7347d7fa46298b3"
    const val DATABASE_NAME = "WeatherDatabase"
    const val FIRST_TIME = "first"
    const val IS_MAP = "map"

    fun convertLongToDayDate(time: Long): String {
        val format = SimpleDateFormat("EEEE")
        return format.format(Date(time*1000))
    }

    fun convertLongToTime(time: Long): String {
        val format = SimpleDateFormat("hh:mm aa")
        return format.format(Date(time*1000))
    }

    fun convertCurrentDate(): String {
        val date = Calendar.getInstance().time
        val format = SimpleDateFormat("dd/MM/yyyy")
        return format.format(date)
    }
}