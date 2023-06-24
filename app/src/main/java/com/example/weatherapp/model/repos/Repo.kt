package com.example.weatherapp.model.repos

import com.example.weatherapp.model.pojo.WeatherResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface Repo {
    fun addBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    suspend fun getCurrentWeather(
        lat: String,
        long: String,
        language: String,
        units: String
    ): Response<WeatherResponse>

    suspend fun insertWeather(weatherResponse: WeatherResponse)
    fun getAllWeather(): Flow<List<WeatherResponse>>
    suspend fun deleteWeather(weatherResponse: WeatherResponse)
}