package com.example.weatherapp.model.pojo

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class WeatherResponse(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var location: String?,
    val current: Current?,
    val daily: List<Daily>?,
    val hourly: List<Hourly>?,
    val lat: Double?,
    val lon: Double?,
    val timezone: String?,
    val timezone_offset: Int?,
    val alerts: List<Alert>?
): Parcelable

@Parcelize
data class Alert (
    @SerializedName("sender_name")
    val senderName: String?,
    val event: String?,
    val start: Long?,
    val end: Long?,
    val description: String?,
    val tags: List<String>?
): Parcelable

@Parcelize
data class Weather(
    val description: String?,
    val icon: String?,
    val id: Int?,
    val main: String?
): Parcelable

@Parcelize
data class Temp(
    val day: Double?,
    val eve: Double?,
    val max: Double?,
    val min: Double?,
    val morn: Double?,
    val night: Double?
): Parcelable

@Parcelize
data class Hourly(
    val clouds: Int?,
    val dew_point: Double?,
    val dt: Long?,
    val feels_like: Double?,
    val humidity: Int?,
    val pop: Double?,
    val pressure: Int?,
    val temp: Double?,
    val uvi: Double?,
    val visibility: Int?,
    val weather: List<Weather>?,
    val wind_deg: Int?,
    val wind_gust: Double?,
    val wind_speed: Double?
): Parcelable

@Parcelize
data class FeelsLike(
    val day: Double?,
    val eve: Double?,
    val morn: Double?,
    val night: Double?
): Parcelable

@Parcelize
data class Daily(
    val clouds: Int?,
    val dew_point: Double?,
    val dt: Long?,
    val feels_like: FeelsLike?,
    val humidity: Int?,
    val moon_phase: Double?,
    val moonrise: Int?,
    val moonset: Int?,
    val pop: Double?,
    val pressure: Int?,
    val rain: Double?,
    val sunrise: Int?,
    val sunset: Int?,
    val temp: Temp?,
    val uvi: Double?,
    val weather: List<Weather>?,
    val wind_deg: Int?,
    val wind_gust: Double?,
    val wind_speed: Double?
): Parcelable

@Parcelize
data class Current(
    val clouds: Int?,
    val dew_point: Double?,
    val dt: Long?,
    val feels_like: Double?,
    val humidity: Int?,
    val pressure: Int?,
    val sunrise: Int?,
    val sunset: Int?,
    val temp: Double?,
    val uvi: Double?,
    val visibility: Int?,
    val weather: List<Weather>?,
    val wind_deg: Int?,
    val wind_gust: Double?,
    val wind_speed: Double?
): Parcelable