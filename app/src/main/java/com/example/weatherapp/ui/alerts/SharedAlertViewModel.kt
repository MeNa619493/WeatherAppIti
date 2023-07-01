package com.example.weatherapp.ui.alerts

import androidx.lifecycle.*
import com.example.weatherapp.model.pojo.WeatherAlert
import com.example.weatherapp.model.repos.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedAlertViewModel @Inject constructor(
    private val repo: Repo
) : ViewModel() {

    private val _alertId: MutableLiveData<Int> = MutableLiveData()
    val alertId: LiveData<Int> = _alertId

    fun saveWeatherAlert(weatherAlert: WeatherAlert) {
        viewModelScope.launch {
            val id = repo.insertAlert(weatherAlert)
            _alertId.postValue(id.toInt())
        }
    }

    fun getAllAlerts() : LiveData<List<WeatherAlert>> {
        return repo.getAllAerts().asLiveData()
    }
}