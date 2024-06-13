package com.dev.weatherapp.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Geocoder
import android.os.Build
import android.util.DisplayMetrics
import com.dev.weatherapp.R
import com.dev.weatherapp.model.data.local.HelperSharedPreferences
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    const val API_KEY = "cad8edbd7bf4c844f7347d7fa46298b3"
    const val DATABASE_NAME = "WeatherDatabase"
    const val FIRST_TIME = "first"
    const val IS_MAP = "map"

    const val LAT = "lat"
    const val LONG = "long"
    const val LANGUAGE = "language"

    const val UNIT = "unit"
    const val METRIC = "metric"
    const val IMPERIAL = "imperial"
    const val STANDARD = "standard"

    const val NOTIFICATION_NAME = "Weather"
    const val NOTIFICATION_CHANNEL = "Weather_channel_01"

    const val ALERT_ID = "id"

    fun convertLongToDayName(time: Long): String {
        val format = SimpleDateFormat("EEEE", Locale.getDefault())
        return format.format(Date(time * 1000))
    }

    fun convertLongToTime(time: Long): String {
        val format = SimpleDateFormat("hh:mm aa",Locale.getDefault())
        return format.format(Date(time * 1000))
    }

     fun setLocale(language: String, context: Context) {
        val myLocale = Locale(language)
        Locale.setDefault(myLocale)
        val res: Resources = context.resources
        val dm: DisplayMetrics = res.displayMetrics
        val conf: Configuration = res.configuration
        conf.locale = myLocale
        conf.setLayoutDirection(myLocale)
        res.updateConfiguration(conf, dm)
    }

    fun convertLongToDayDate(time: Long): String {
        val format = SimpleDateFormat("d MMM, yyyy", Locale.getDefault())
        return format.format(time * 1000)
    }

    fun getDateMillis(date: String): Long {
        val f = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val d: Date = f.parse(date)
        return d.time
    }

    fun convertLongToDayDateAlert(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(date)
    }

    fun convertLongToTimePicker(time: Long): String {
        val date = Date(time - 7200000)
        val format = SimpleDateFormat("h:mm aa", Locale.getDefault())
        return format.format(date)
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

    fun getAddress(context: Context, lat: Double, lon: Double): String {
        var address = " "
        val geocoder = Geocoder(context, Locale.getDefault())
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