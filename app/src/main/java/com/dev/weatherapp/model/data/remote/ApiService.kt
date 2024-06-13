package com.dev.weatherapp.model.data.remote

import com.dev.weatherapp.model.pojo.WeatherResponse
import com.dev.weatherapp.utils.Utils
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("onecall")
    suspend fun getCurrentWeather(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("units") units: String,
        @Query("lang") lang: String,
        @Query("appid") app_id: String = Utils.API_KEY
    ): Response<WeatherResponse>
}