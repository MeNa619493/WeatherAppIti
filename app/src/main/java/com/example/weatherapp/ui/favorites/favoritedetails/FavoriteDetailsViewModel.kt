package com.example.weatherapp.ui.favorites.favoritedetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.pojo.WeatherResponse
import com.example.weatherapp.model.data.repos.Repo
import com.example.weatherapp.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteDetailsViewModel @Inject constructor(
    private val repo: Repo,
) : ViewModel() {

    private val _weather: MutableLiveData<NetworkResult<WeatherResponse>> = MutableLiveData()
    val weather: LiveData<NetworkResult<WeatherResponse>> = _weather

    fun getFavWeather(
        lat: String,
        long: String,
        units: String,
        language: String
    ) {
        _weather.value = NetworkResult.Loading()
        viewModelScope.launch {
            val weatherResponse = repo.getCurrentWeather(lat, long, units, language)
            if (weatherResponse.isSuccessful) {
                weatherResponse.body()?.let {
                    _weather.postValue(NetworkResult.Success(it))
                }
            } else {
                //Log.e("FavoriteDetailViewModel", weatherResponse.errorBody().toString())
                _weather.postValue(NetworkResult.Error(weatherResponse.message()))
            }
        }
    }
}