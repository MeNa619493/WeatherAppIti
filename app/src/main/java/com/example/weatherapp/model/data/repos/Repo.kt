package com.example.weatherapp.model.data.repos

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherapp.model.pojo.WeatherAlert
import com.example.weatherapp.model.pojo.WeatherResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface Repo {
    suspend fun getCurrentWeather(
        lat: String,
        long: String,
        units: String,
        language: String
    ): Response<WeatherResponse>

    suspend fun insertWeather(weatherResponse: WeatherResponse)
    suspend fun getCurrentWeather(): WeatherResponse
    fun getAllWeather(): Flow<List<WeatherResponse>>
    suspend fun deleteCurrentWeather()
    suspend fun deleteWeather(weatherResponse: WeatherResponse)
    suspend fun insertAlert(weatherAlert: WeatherAlert): Long
    fun getAllAerts(currentTime: Long): Flow<List<WeatherAlert>>
    suspend fun deleteAlerts(currentTime: Long)
    suspend fun deleteAlert(weatherAlert: WeatherAlert): Int
}