package com.example.weatherapp.ui

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.*
import com.example.weatherapp.model.pojo.WeatherResponse
import com.example.weatherapp.model.repos.Repo
import com.example.weatherapp.ui.home.HomeFragment
import com.example.weatherapp.utils.Constants
import com.example.weatherapp.utils.NetworkResult
import com.google.android.gms.location.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val repo: Repo,
    private val mFusedLocationProviderClient: FusedLocationProviderClient
) : ViewModel() {

    val locationLiveData: MutableLiveData<Location> = MutableLiveData()

    val favLocationState: MutableStateFlow<Location> = MutableStateFlow(Location("start"))

    private val _weather: MutableLiveData<NetworkResult<WeatherResponse>> = MutableLiveData()
    val weather: LiveData<NetworkResult<WeatherResponse>> = _weather

    fun getIsMapBoolean(key: String, defaultValue: Boolean): Boolean {
        return repo.getBoolean(key, defaultValue)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val myLocation = locationResult.lastLocation
            myLocation?.let {
                locationLiveData.value = it
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
        language: String,
        units: String
    ) {
        _weather.value = NetworkResult.Loading()
        viewModelScope.launch {
            val weatherResponse = repo.getCurrentWeather(lat, long, language, units)
            if (weatherResponse.isSuccessful) {
                repo.addString(Constants.LAT, lat)
                repo.addString(Constants.LONG, long)
                weatherResponse.body()?.let {
                    _weather.postValue(NetworkResult.Success(it))
                }
            } else {
                Log.e("SharedViewModel", weatherResponse.errorBody().toString())
                _weather.postValue(NetworkResult.Error(weatherResponse.message()))
            }
        }
    }

    fun saveFavLocationWeather(
        lat: String,
        long: String,
        language: String,
        units: String,
        address: String
    ) {
        viewModelScope.launch {
            val weatherResponse = repo.getCurrentWeather(lat, long, language, units)
            if (weatherResponse.isSuccessful) {
                weatherResponse.body()?.let {
                    it.location = address
                    repo.insertWeather(it)
                }
            } else {
                Log.e("MealsViewModel", weatherResponse.errorBody().toString())
            }
        }
    }

    fun getAllFavs() : LiveData<List<WeatherResponse>> {
        return repo.getAllWeather().asLiveData()
    }

    fun deleteFav(weatherResponse: WeatherResponse) {
        viewModelScope.launch {
            repo.deleteWeather(weatherResponse)
        }
    }
}