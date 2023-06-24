package com.example.weatherapp.model.repos

import com.example.weatherapp.model.local.HelperSharedPreferences
import com.example.weatherapp.model.local.WeatherDao
import com.example.weatherapp.model.pojo.WeatherResponse
import com.example.weatherapp.model.remote.ApiService
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class RepoImpl(
    private val apiService: ApiService,
    private val dao: WeatherDao,
    private val sharedPreferences: HelperSharedPreferences
) : Repo {
    override fun addBoolean(key: String, value: Boolean) {
        sharedPreferences.addBoolean(key, value)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    override suspend fun getCurrentWeather(
        lat: String,
        long: String,
        language: String,
        units: String
    ): Response<WeatherResponse> {
        return apiService.getCurrentWeather(lat, long, language, units)
    }

    override suspend fun insertWeather(weatherResponse: WeatherResponse) {
        dao.insertWeather(weatherResponse)
    }

    override fun getAllWeather(): Flow<List<WeatherResponse>> {
        return dao.getAllWeather()
    }

    override suspend fun deleteWeather(weatherResponse: WeatherResponse) {
        dao.deleteWeather(weatherResponse)
    }


}