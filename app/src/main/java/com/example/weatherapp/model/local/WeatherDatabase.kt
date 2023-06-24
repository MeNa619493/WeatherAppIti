package com.example.weatherapp.model.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.weatherapp.model.pojo.WeatherResponse

@Database(entities = [WeatherResponse::class], version = 1, exportSchema = false)
@TypeConverters(WeatherTypeConverter::class)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherDao(): WeatherDao

}