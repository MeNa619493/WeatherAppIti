package com.example.weatherapp.model.local

import androidx.room.*
import com.example.weatherapp.model.pojo.Alert
import com.example.weatherapp.model.pojo.WeatherAlert
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(weatherAlert: WeatherAlert): Long

    @Query("Select * from Alert")
    fun getAllAerts(): Flow<List<WeatherAlert>>

    @Delete
    suspend fun deleteAlert(weatherAlert: WeatherAlert)
}