package com.example.weatherapp.model.local

import androidx.room.*
import com.example.weatherapp.model.pojo.WeatherResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weatherResponse: WeatherResponse)

    @Query("Select * from WeatherResponse")
    fun getAllWeather(): Flow<List<WeatherResponse>>

    @Delete
    suspend fun deleteWeather(weatherResponse: WeatherResponse)
}