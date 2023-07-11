package com.example.weatherapp.ui

import android.annotation.SuppressLint
import android.os.Looper
import android.util.Log
import androidx.lifecycle.*
import com.example.weatherapp.model.pojo.UserLocation
import com.example.weatherapp.model.pojo.WeatherResponse
import com.example.weatherapp.model.data.repos.Repo
import com.example.weatherapp.utils.NetworkResult
import com.google.android.gms.location.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val repo: Repo,
    private val mFusedLocationProviderClient: FusedLocationProviderClient
) : ViewModel() {

    val locationLiveData: MutableLiveData<UserLocation> = MutableLiveData()

    private val _weather: MutableLiveData<NetworkResult<WeatherResponse>> = MutableLiveData()
    val weather: LiveData<NetworkResult<WeatherResponse>> = _weather

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val myLocation = locationResult.lastLocation
            myLocation?.let {
                val location = UserLocation(it.latitude, it.longitude)
                locationLiveData.value = location
            }
            stopLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    fun requestNewLocationData() {
        Log.d("SharedViewModel", "requestNewLocationData is here")
        val mLocationRequest = LocationRequest().apply {
            setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            setInterval(0)
        }

        mFusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    private fun stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    fun getCurrentWeather(
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
                    repo.deleteCurrentWeather()
                    repo.insertWeather(it)
                    _weather.postValue(NetworkResult.Success(it))
                }
            } else {
                //Log.e("SharedViewModel", weatherResponse.errorBody().toString())
                _weather.postValue(NetworkResult.Success(repo.getCurrentWeather()))
            }
        }
    }

    fun getCachedWeather() {
        _weather.value = NetworkResult.Loading()
        viewModelScope.launch {
            _weather.postValue(NetworkResult.Success(repo.getCurrentWeather()))
        }
    }

    fun saveFavLocationWeather(
        lat: String,
        long: String,
        units: String,
        language: String,
    ) {
        viewModelScope.launch {
            val weatherResponse = repo.getCurrentWeather(lat, long, units, language)
            if (weatherResponse.isSuccessful) {
                weatherResponse.body()?.let {
                    it.isFavourite = true
                    repo.insertWeather(it)
                }
            } else {
                Log.e("SharedViewModel", weatherResponse.errorBody().toString())
            }
        }
    }

    fun addFavLocation(weatherResponse: WeatherResponse) =
        viewModelScope.launch {
            repo.insertWeather(weatherResponse)
        }

    fun getAllFavs(): LiveData<List<WeatherResponse>> {
        return repo.getAllWeather().asLiveData()
    }

    fun deleteFav(weatherResponse: WeatherResponse) =
        viewModelScope.launch {
            repo.deleteWeather(weatherResponse)
        }

}