package com.example.weatherapp.model.repos

import com.example.weatherapp.model.local.HelperSharedPreferences
import com.example.weatherapp.model.local.WeatherDao
import com.example.weatherapp.model.remote.ApiService

class RepoImpl(
    private val apiService: ApiService,
    private val dao: WeatherDao,
    private val sharedPreferences: HelperSharedPreferences
) : Repo {
    override fun addBoolean(key: String, value: Boolean) {
        sharedPreferences.addBoolean(key, value)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
}