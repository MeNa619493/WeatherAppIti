package com.example.weatherapp.model.pojo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Alert")
data class WeatherAlert (
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var timeFrom: Long,
    var timeTo: Long,
    var startDate: Long,
    var endDate: Long
)

