package com.example.weatherapp.model.data.local

import androidx.room.TypeConverter
import com.example.weatherapp.model.pojo.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WeatherTypeConverter {
    var gson = Gson()

    @TypeConverter
    fun currentToJson(current: Current?) = Gson().toJson(current)

    @TypeConverter
    fun jsonToCurrent(currentString: String) =
        Gson().fromJson(currentString, Current::class.java)

    @TypeConverter
    fun hourlyListToJson(hourlyList: List<Hourly>?) = Gson().toJson(hourlyList)

    @TypeConverter
    fun jsonToHourlyList(hourlyString: String?): List<Hourly>? {
        hourlyString?.let {
            return Gson().fromJson(it, Array<Hourly>::class.java)?.toList()
        }
        return emptyList()
    }

    @TypeConverter
    fun dailyListToJson(dailyList: List<Daily>?) = Gson().toJson(dailyList)

    @TypeConverter
    fun jsonToDailyList(dailyString: String?): List<Daily>? {
        dailyString?.let {
            return Gson().fromJson(it, Array<Daily>::class.java)?.toList()
        }
        return emptyList()
    }

    @TypeConverter
    fun weatherListToJson(weatherList: List<Weather>?) = Gson().toJson(weatherList)

    @TypeConverter
    fun jsonToWeatherList(weatherString: String?): List<Weather>? {
        weatherString?.let {
            return Gson().fromJson(it, Array<Weather>::class.java)?.toList()
        }
        return emptyList()
    }

    @TypeConverter
    fun alertListToJson(alertList: List<Alert>?) = Gson().toJson(alertList)

    @TypeConverter
    fun jsonToAlertList(alertString: String?): List<Alert>? {
        alertString?.let {
            return Gson().fromJson(it, Array<Alert>::class.java)?.toList()
        }
        return emptyList()
    }
}