package com.dev.weatherapp.model.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dev.weatherapp.model.pojo.WeatherAlert
import com.dev.weatherapp.model.pojo.WeatherResponse

@Database(entities = [WeatherResponse::class, WeatherAlert::class], version = 1, exportSchema = false)
@TypeConverters(WeatherTypeConverter::class)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherDao(): WeatherDao

    abstract fun alertDao(): AlertDao

}