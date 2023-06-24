package com.example.weatherapp.model.local

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class HelperSharedPreferences @Inject constructor(private val context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)
    }

    fun addString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    fun addBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun deleteData(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
}