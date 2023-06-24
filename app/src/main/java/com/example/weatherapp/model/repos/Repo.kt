package com.example.weatherapp.model.repos

interface Repo {
    fun addBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
}