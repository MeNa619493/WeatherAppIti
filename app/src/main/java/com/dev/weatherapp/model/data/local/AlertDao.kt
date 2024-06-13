package com.dev.weatherapp.model.data.local

import androidx.room.*
import com.dev.weatherapp.model.pojo.Alert
import com.dev.weatherapp.model.pojo.WeatherAlert
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(weatherAlert: WeatherAlert): Long

    @Query("Select * from Alert WHERE endDate + timeTo > :currentTime")
    fun getAllAerts(currentTime: Long): Flow<List<WeatherAlert>>

    @Query("Delete FROM Alert WHERE endDate + timeTo < :currentTime")
    suspend fun deleteAlerts(currentTime: Long)

    @Delete
    suspend fun deleteAlert(weatherAlert: WeatherAlert): Int
}