package com.example.weatherapp.model.local

import androidx.room.TypeConverter
import com.example.weatherapp.model.pojo.Current
import com.example.weatherapp.model.pojo.Daily
import com.example.weatherapp.model.pojo.Hourly
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WeatherTypeConverter {
    var gson = Gson()

    @TypeConverter
    fun currentToString(current: Current): String {
        return gson.toJson(current)
    }

    @TypeConverter
    fun stringToCurrent(data: String): Current {
        val listType = object : TypeToken<Current>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun hourlyToString(hourly: List<Hourly>): String {
        return gson.toJson(hourly)
    }

    @TypeConverter
    fun stringToHourly(data: String): List<Hourly> {
        val listType = object : TypeToken<List<Hourly>>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun dailyToString(daily: List<Daily>): String {
        return gson.toJson(daily)
    }

    @TypeConverter
    fun stringToDaily(data: String): List<Daily> {
        val listType = object : TypeToken<List<Daily>>() {}.type
        return gson.fromJson(data, listType)
    }
}