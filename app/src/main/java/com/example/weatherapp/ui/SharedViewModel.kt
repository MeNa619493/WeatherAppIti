package com.example.weatherapp.ui

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherapp.model.repos.Repo
import com.example.weatherapp.ui.home.HomeFragment
import com.google.android.gms.location.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val repo: Repo,
    private val mFusedLocationProviderClient: FusedLocationProviderClient
): ViewModel() {

    val locationLiveData: MutableLiveData<Location> = MutableLiveData()

    fun getIsMapBoolean(key: String, defaultValue: Boolean): Boolean {
        return repo.getBoolean(key, defaultValue)
    }

    private val locationCallback =  object : LocationCallback() {
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
}