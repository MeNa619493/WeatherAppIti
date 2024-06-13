package com.dev.weatherapp.model.data.local

import androidx.room.*
import com.dev.weatherapp.model.pojo.WeatherResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weatherResponse: WeatherResponse)

    @Query("Select * from WeatherResponse where isFavourite = 0")
    suspend fun getCurrentWeather(): WeatherResponse

    @Query("Delete from WeatherResponse where isFavourite = 0")
    suspend fun deleteCurrentWeather()

    @Query("Select * from WeatherResponse where isFavourite = 1")
    fun getAllWeather(): Flow<List<WeatherResponse>>

    @Delete
    suspend fun deleteWeather(weatherResponse: WeatherResponse)
}