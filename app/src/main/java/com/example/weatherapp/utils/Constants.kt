package com.example.weatherapp.utils

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.example.weatherapp.R
import com.example.weatherapp.model.local.HelperSharedPreferences
import com.example.weatherapp.model.pojo.UserLocation
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object Constants {
    const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    const val API_KEY = "cad8edbd7bf4c844f7347d7fa46298b3"
    const val DATABASE_NAME = "WeatherDatabase"
    const val FIRST_TIME = "first"
    const val IS_MAP = "map"
    const val DESCRIPTION = "description"
    const val ICON = "icon"
    const val LAT = "lat"
    const val LONG = "long"
    const val LANGUAGE = "language"
    const val UNIT = "unit"

    const val METRIC = "metric"
    const val IMPERIAL = "imperial"
    const val STANDARD = "standard"

    const val NOTIFICATION_NAME: String = "Weather"
    const val NOTIFICATION_CHANNEL: String = "Weather_channel_01"

    fun convertLongToDayName(time: Long, language:String): String {
        val format = SimpleDateFormat("EEEE", Locale(language))
        return format.format(Date(time * 1000))
    }

    fun convertLongToTime(time: Long, language:String): String {
        val format = SimpleDateFormat("hh:mm aa", Locale(language))
        return format.format(Date(time * 1000))
    }

    fun convertLongToDayDate(time: Long, language:String): String {
        val format = SimpleDateFormat("d MMM, yyyy", Locale(language))
        return format.format(time * 1000)
    }

    fun getDateMillis(date: String, language:String): Long {
        val f = SimpleDateFormat("dd/MM/yyyy", Locale(language))
        val d: Date = f.parse(date)
        return d.time
    }

    fun convertLongToDayDateAlert(time: Long, language:String): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd/MM/yyyy", Locale(language))
        return format.format(date)
    }

    fun convertLongToTimePicker(time: Long, language:String): String {
        val date = Date(time - 7200000)
        val format = SimpleDateFormat("h:mm aa", Locale(language))
        return format.format(date)
    }

    fun getCurrentLocale(context: Context): Locale? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
    }

    fun getSpeedUnit(context: Context): String {
        val sharedPreference = HelperSharedPreferences(context)
        return when (sharedPreference.getString(UNIT, METRIC)) {
            IMPERIAL -> {
                context.getString(R.string.m_h)
            }
            else -> {
                context.getString(R.string.m_s)
            }
        }
    }

    fun getTemperatureUnit(context: Context): String {
        val sharedPreference = HelperSharedPreferences(context)
        return when (sharedPreference.getString(UNIT, METRIC)) {
            IMPERIAL -> {
                context.getString(R.string.f)
            }
            STANDARD -> {
                context.getString(R.string.k)
            }
            else -> {
                context.getString(R.string.c)
            }
        }
    }

    fun getAddress(context: Context, lat: Double, lon: Double, language:String): String {
        var address = " "
        val geocoder = Geocoder(context, Locale(language))
        try {
            val addressList = geocoder.getFromLocation(lat, lon, 1)
            if (addressList != null) {
                if (addressList.isNotEmpty()) {
                    address = "${addressList[0].adminArea}, ${addressList[0].countryName}"
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return address
    }
}