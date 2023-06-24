package com.example.weatherapp.ui.dialog

import androidx.lifecycle.ViewModel
import com.example.weatherapp.model.repos.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DialogViewModel @Inject constructor(private val repo: Repo) : ViewModel() {

    fun putIsMapBoolean(key: String, value: Boolean) {
        repo.addBoolean(key, value)
    }
}