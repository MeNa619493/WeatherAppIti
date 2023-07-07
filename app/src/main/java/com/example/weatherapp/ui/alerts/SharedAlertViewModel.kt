package com.example.weatherapp.ui.alerts

import androidx.lifecycle.*
import com.example.weatherapp.model.pojo.WeatherAlert
import com.example.weatherapp.model.repos.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedAlertViewModel @Inject constructor(
    private val repo: Repo
) : ViewModel() {

    private val _alertInsertedSuccess: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val alertInsertedSuccess: MutableStateFlow<Boolean> = _alertInsertedSuccess

    private val _deletedAlertId: MutableStateFlow<Int> = MutableStateFlow(0)
    val deletedAlertId: MutableStateFlow<Int> = _deletedAlertId


    fun saveWeatherAlert(weatherAlert: WeatherAlert) {
        viewModelScope.launch {
            val id = repo.insertAlert(weatherAlert)
            _alertInsertedSuccess.value = id>=0
        }
    }

    fun getAllAlerts() : LiveData<List<WeatherAlert>> {
        return repo.getAllAerts().asLiveData()
    }

    fun deleteAlert(weatherAlert: WeatherAlert) = viewModelScope.launch {
        val id = repo.deleteAlert(weatherAlert)
        _deletedAlertId.value = id
    }
}