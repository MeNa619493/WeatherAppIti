package com.example.weatherapp.utils

import java.text.SimpleDateFormat
import java.util.*

object Constants {
    const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    const val API_KEY = "cad8edbd7bf4c844f7347d7fa46298b3"
    const val DATABASE_NAME = "WeatherDatabase"
    const val FIRST_TIME = "first"
    const val IS_MAP = "map"
    const val ALERT_ID = "id"
    const val DESCRIPTION = "description"
    const val ICON = "icon"
    const val LAT = "lat"
    const val LONG = "long"

    const val NOTIFICATION_NAME: String = "Weather"
    const val NOTIFICATION_CHANNEL: String = "Weather_channel_01"

    fun convertLongToDayName(time: Long): String {
        val format = SimpleDateFormat("EEEE")
        return format.format(Date(time*1000))
    }

    fun convertLongToTime(time: Long): String {
        val format = SimpleDateFormat("hh:mm aa")
        return format.format(Date(time*1000))
    }

    fun convertLongToDayDate(time: Long): String {
        val format = SimpleDateFormat("d MMM, yyyy")
        return format.format(time*1000)
    }

    fun getDateMillis(date: String): Long {
        val f = SimpleDateFormat("dd/MM/yyyy")
        val d: Date = f.parse(date)
        return d.time
    }

    fun convertLongToDayDateAlert(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd/MM/yyyy")
        return format.format(date)
    }

    fun convertLongToTimePicker(time: Long): String {
        val date = Date(time-7200000)
        val format = SimpleDateFormat("h:mm aa")
        return format.format(date)
    }
}