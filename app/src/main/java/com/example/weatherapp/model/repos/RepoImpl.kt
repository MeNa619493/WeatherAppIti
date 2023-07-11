package com.example.weatherapp.model.repos

import com.example.weatherapp.model.local.AlertDao
import com.example.weatherapp.model.local.HelperSharedPreferences
import com.example.weatherapp.model.local.WeatherDao
import com.example.weatherapp.model.pojo.WeatherAlert
import com.example.weatherapp.model.pojo.WeatherResponse
import com.example.weatherapp.model.remote.ApiService
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class RepoImpl(
    private val apiService: ApiService,
    private val weatherDao: WeatherDao,
    private val alertDao: AlertDao,
) : Repo {

    override suspend fun getCurrentWeather(
        lat: String,
        long: String,
        units: String,
        language: String
    ): Response<WeatherResponse> {
        return apiService.getCurrentWeather(lat, long, units, language)
    }

    override suspend fun insertWeather(weatherResponse: WeatherResponse) {
        weatherDao.insertWeather(weatherResponse)
    }

    override fun getAllWeather(): Flow<List<WeatherResponse>> {
        return weatherDao.getAllWeather()
    }

    override suspend fun deleteWeather(weatherResponse: WeatherResponse) {
        weatherDao.deleteWeather(weatherResponse)
    }

    override suspend fun insertAlert(weatherAlert: WeatherAlert): Long {
        return alertDao.insertAlert(weatherAlert)
    }

    override fun getAllAerts(currentTime: Long): Flow<List<WeatherAlert>> {
        return alertDao.getAllAerts(currentTime)
    }

    override suspend fun deleteAlerts(currentTime: Long) {
        return alertDao.deleteAlerts(currentTime)
    }

    override suspend fun deleteAlert(weatherAlert: WeatherAlert): Int {
        return alertDao.deleteAlert(weatherAlert)
    }
}