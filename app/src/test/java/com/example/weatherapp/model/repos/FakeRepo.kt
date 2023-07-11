package com.example.weatherapp.model.repos

import com.example.weatherapp.model.data.repos.Repo
import com.example.weatherapp.model.pojo.WeatherAlert
import com.example.weatherapp.model.pojo.WeatherResponse
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
            Response.error(400, ResponseBody.create(
                MediaType.parse("application/json"),
                language.toByteArray()))
        } else {
            Response.success(weatherData[0])
        }
    }

    override suspend fun insertWeather(weatherResponse: WeatherResponse) {
        weatherData.add(weatherResponse)
    }

    override fun getAllWeather(): Flow<List<WeatherResponse>> {
        return flowOf(weatherData)
    }

    override suspend fun deleteWeather(weatherResponse: WeatherResponse) {
        weatherData.remove(weatherResponse)
    }

    override suspend fun insertAlert(weatherAlert: WeatherAlert): Long {
        alertData.add(weatherAlert)
        return weatherAlert.id!!.toLong()
    }

    override fun getAllAerts(): Flow<List<WeatherAlert>> {
        return flowOf(alertData)
    }

    override suspend fun deleteAlert(weatherAlert: WeatherAlert): Int {
        alertData.remove(weatherAlert)
        return weatherAlert.id!!
    }
}