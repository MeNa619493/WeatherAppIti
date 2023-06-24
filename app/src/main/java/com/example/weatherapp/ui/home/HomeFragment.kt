package com.example.weatherapp.ui.home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavAction
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.model.pojo.WeatherResponse
import com.example.weatherapp.ui.SharedViewModel
import com.example.weatherapp.ui.home.adapters.DailyAdapter
import com.example.weatherapp.ui.home.adapters.HourlyAdapter
import com.example.weatherapp.utils.Constants
import com.example.weatherapp.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel
    private lateinit var geoCoder: Geocoder

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val hourlyAdapter by lazy { HourlyAdapter() }
    private val dailyAdapter by lazy { DailyAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        geoCoder = Geocoder(requireContext())

        if (viewModel.locationLiveData.value == null) {
            getLastLocation()
        }

        viewModel.locationLiveData.observe(viewLifecycleOwner) {
            Log.d(TAG, "onViewCreated: location is here")
            showAddress(it)
            viewModel.getCurrentWeather("${it.latitude}", "${it.longitude}","en", "standard")
        }

        observeWeatherResponse()
        setupHourlyRecyclerView()
        setupDailyRecyclerView()
    }

    private fun getLastLocation() {
        if (checkPermission()){
            if (isLocationEnabled()){
                viewModel.requestNewLocationData()
                if (viewModel.getIsMapBoolean(Constants.IS_MAP, false)) {
                    Log.d(TAG, "onViewCreated: Map is here")
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToMapFragment(false))
                }
                Log.d(TAG, "onViewCreated: GPS is here")
            } else {
                enableLocationSetting()
            }
        } else {
            requestPermission()
        }
    }

    private fun checkPermission() = ActivityCompat.checkSelfPermission(
        requireActivity(),
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED


    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_ID){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastLocation()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun enableLocationSetting() {
        Toast.makeText(requireActivity(), "Turn on Location", Toast.LENGTH_SHORT).show()
        val settingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        activity?.startActivity(settingIntent)
    }

    private fun showAddress(location: Location) {
        try {
            val addressList = geoCoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )
            addressList?.let {
                binding.tvAddress.text = "${it[0].countryName}, ${it[0].adminArea}"
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun observeWeatherResponse() {
        viewModel.weather.observe(viewLifecycleOwner) { response ->
            Log.d(TAG, "observeWeatherResponse: ${response.data}")
            when (response) {
                is NetworkResult.Success -> {
                    response.data?.let {
                        hourlyAdapter.submitList(it.hourly)
                        dailyAdapter.submitList(it.daily)
                        initUi(it)
                    }
                }
                is NetworkResult.Error -> {

                }
                is NetworkResult.Loading -> {

                }
            }
        }
    }

    private fun initUi(weatherResponse: WeatherResponse) {
        binding.apply {
            tvDate.text = Constants.convertCurrentDate()
            weatherResponse.current?.weather?.get(0)?.let {
                tvWeatherDesc.text = it.description

                Glide
                    .with(binding.root)
                    .load("https://openweathermap.org/img/wn/${it.icon}@2x.png")
                    .into(ivWeather);

            }
            tvTemp.text = weatherResponse.current?.temp.toString()
            tvPressureDeg.text = weatherResponse.current?.pressure.toString()
            tvWindDeg.text = weatherResponse.current?.wind_speed.toString()
            tvHumidityDeg.text = weatherResponse.current?.humidity.toString()
            tvCloudDeg.text = weatherResponse.current?.clouds.toString()
            tvRayDeg.text = weatherResponse.current?.uvi.toString()
            tvVisibilityDeg.text = weatherResponse.current?.visibility.toString()
        }
    }

    private fun setupHourlyRecyclerView() {
        binding.rvHourly.apply {
            adapter = hourlyAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupDailyRecyclerView() {
        binding.rvDaily.apply {
            adapter = dailyAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "HomeFragment"
        private const val PERMISSION_ID = 44
    }
}

