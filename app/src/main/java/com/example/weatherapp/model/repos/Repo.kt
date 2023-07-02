package com.example.weatherapp.model.repos

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherapp.model.pojo.WeatherAlert
import com.example.weatherapp.model.pojo.WeatherResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface Repo {
    fun addBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun addString(key: String, value: String)
    fun getString(key: String, defaultValue: String): String
    suspend fun getCurrentWeather(
        lat: String,
        long: String,
        units: String,
        language: String
    ): Response<WeatherResponse>

    suspend fun insertWeather(weatherResponse: WeatherResponse)
    fun getAllWeather(): Flow<List<WeatherResponse>>
    suspend fun deleteWeather(weatherResponse: WeatherResponse)
    suspend fun insertAlert(weatherAlert: WeatherAlert): Long
    fun getAllAerts(): Flow<List<WeatherAlert>>
    suspend fun deleteAlert(weatherAlert: WeatherAlert)
}