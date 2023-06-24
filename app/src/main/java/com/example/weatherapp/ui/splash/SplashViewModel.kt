package com.example.weatherapp.ui.splash

import androidx.lifecycle.ViewModel
import com.example.weatherapp.model.repos.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private val repo: Repo) : ViewModel() {

    fun getFirstTimeBoolean(key: String, defaultValue: Boolean): Boolean {
        return repo.getBoolean(key, defaultValue)
    }
}