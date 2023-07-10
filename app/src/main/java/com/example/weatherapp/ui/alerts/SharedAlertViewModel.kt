package com.example.weatherapp.ui.alerts

import androidx.lifecycle.*
import com.example.weatherapp.model.pojo.WeatherAlert
import com.example.weatherapp.model.repos.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedAlertViewModel @Inject constructor(
    private val repo: Repo
) : ViewModel() {

    private val _alertInsertedSuccess = MutableSharedFlow<Boolean>()
    val alertInsertedSuccess: Flow<Boolean> = _alertInsertedSuccess.asSharedFlow()

    private val _deletedAlertId = MutableSharedFlow<Int>()
    val deletedAlertId: Flow<Int> = _deletedAlertId.asSharedFlow()

    fun saveWeatherAlert(weatherAlert: WeatherAlert) {
        viewModelScope.launch {
            val id = repo.insertAlert(weatherAlert)
            _alertInsertedSuccess.emit(id>=0)
        }
    }

    fun getAllAlerts() : LiveData<List<WeatherAlert>> {
        return repo.getAllAerts().asLiveData()
    }

    fun deleteAlert(weatherAlert: WeatherAlert) = viewModelScope.launch {
        val id = repo.deleteAlert(weatherAlert)
        _deletedAlertId.emit(id)
    }
}