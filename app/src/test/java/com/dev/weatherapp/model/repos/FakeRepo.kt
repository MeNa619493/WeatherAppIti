package com.dev.weatherapp.model.repos

import com.dev.weatherapp.model.data.repos.Repo
import com.dev.weatherapp.model.pojo.WeatherAlert
import com.dev.weatherapp.model.pojo.WeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Response

class FakeRepo: Repo {

    var weatherData = mutableListOf<WeatherResponse>()
    var alertData = mutableListOf<WeatherAlert>()
    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getCurrentWeather(
        lat: String,
        long: String,
        units: String,
        language: String
    ): Response<WeatherResponse> {
        return if (shouldReturnError) {
            Response.success(getCurrentWeather())
        } else {
            Response.success(weatherData[0])
        }
    }

    override suspend fun getCurrentWeather(): WeatherResponse {
        return weatherData.first { !it.isFavourite }
    }

    override suspend fun insertWeather(weatherResponse: WeatherResponse) {
        weatherData.add(weatherResponse)
    }

    override fun getAllWeather(): Flow<List<WeatherResponse>> {
        return flowOf(weatherData.filter { it.isFavourite })
    }

    override suspend fun deleteCurrentWeather() {
        weatherData.remove(weatherData.first { !it.isFavourite })
    }

    override suspend fun deleteWeather(weatherResponse: WeatherResponse) {
        weatherData.remove(weatherResponse)
    }

    override suspend fun insertAlert(weatherAlert: WeatherAlert): Long {
        alertData.add(weatherAlert)
        return weatherAlert.id!!.toLong()
    }

    override fun getAllAerts(currentTime: Long): Flow<List<WeatherAlert>> {
        return flowOf(alertData.filter { (it.endDate + it.timeTo) > currentTime })
    }

    override suspend fun deleteAlerts(currentTime: Long) {
        alertData.removeIf { (it.endDate + it.timeTo) < currentTime }
    }


    override suspend fun deleteAlert(weatherAlert: WeatherAlert): Int {
        alertData.remove(weatherAlert)
        return weatherAlert.id!!
    }
}