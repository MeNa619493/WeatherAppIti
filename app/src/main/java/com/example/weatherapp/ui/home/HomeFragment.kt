package com.example.weatherapp.ui.home

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import androidx.core.content.ContextCompat.registerReceiver
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.model.local.HelperSharedPreferences
import com.example.weatherapp.model.pojo.UserLocation
import com.example.weatherapp.model.pojo.WeatherResponse
import com.example.weatherapp.ui.SharedViewModel
import com.example.weatherapp.ui.home.adapters.DailyAdapter
import com.example.weatherapp.ui.home.adapters.HourlyAdapter
import com.example.weatherapp.utils.Constants
import com.example.weatherapp.utils.Constants.METRIC
import com.example.weatherapp.utils.Constants.getSpeedUnit
import com.example.weatherapp.utils.Constants.getTemperatureUnit
import com.example.weatherapp.utils.NetworkListener
import com.example.weatherapp.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPreferences: HelperSharedPreferences

    private val hourlyAdapter by lazy {
        HourlyAdapter(
            requireContext(),
            sharedPreferences.getString(Constants.LANGUAGE, "en")
        )
    }
    private val dailyAdapter by lazy {
        DailyAdapter(
            requireContext(),
            sharedPreferences.getString(Constants.LANGUAGE, "en")
        )
    }

    @Inject
    lateinit var networkChangeListener: NetworkListener

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

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(requireActivity(), networkChangeListener, filter, RECEIVER_NOT_EXPORTED)

        observeNetworkState()
        setupHourlyRecyclerView()
        setupDailyRecyclerView()
        observeLocationChange()
        observeWeatherResponse()
    }

    override fun onPause() {
        super.onPause()
        activity?.let {
            LocalBroadcastManager.getInstance(it).unregisterReceiver(networkChangeListener)
        }
    }

    private fun observeNetworkState() {
        NetworkListener.isNetworkAvailable.observe(viewLifecycleOwner) {
            if (it) {
                hideNoConnectionViews()
                if (sharedPreferences.getString(Constants.LAT, "").isBlank()
                    || sharedPreferences.getString(Constants.LONG, "").isBlank()
                ) {
                    getLastLocation()
                } else {
                    val location = UserLocation(
                        sharedPreferences.getString(Constants.LAT, "").toDouble(),
                        sharedPreferences.getString(Constants.LONG, "").toDouble()
                    )
                    viewModel.locationLiveData.value = location
                }
            } else {
                hideAllViews()
                showNoConnectionViews()
            }
        }
    }

    private fun hideNoConnectionViews() {
        binding.apply {
            ivNoConnection.visibility = View.GONE
            tvNoConnection.visibility = View.GONE
        }
    }

    private fun showNoConnectionViews() {
        binding.apply {
            ivNoConnection.visibility = View.VISIBLE
            tvNoConnection.visibility = View.VISIBLE
        }
    }

    private fun hideAllViews() {
        binding.apply {
            tvAddress.visibility = View.GONE
            tvDate.visibility = View.GONE
            llWeatherCard.visibility = View.GONE
            shimmerView.visibility = View.GONE
            rvHourly.visibility = View.GONE
            rvDaily.visibility = View.GONE
        }
    }

    private fun observeLocationChange() {
        viewModel.locationLiveData.observe(viewLifecycleOwner) {
            Log.d(TAG, "onViewCreated: location is here")
            viewModel.getCurrentWeather(
                "${it.lat}",
                "${it.lon}",
                sharedPreferences.getString(Constants.UNIT, "metric"),
                sharedPreferences.getString(Constants.LANGUAGE, "en")
            )
        }
    }

    private fun getLastLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                viewModel.requestNewLocationData()
                if (sharedPreferences.getBoolean(Constants.IS_MAP, false)) {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToMapFragment(
                            false
                        )
                    )
                }
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

        if (requestCode == PERMISSION_ID) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun enableLocationSetting() {
        Toast.makeText(requireActivity(), "Turn on Location", Toast.LENGTH_SHORT).show()
        val settingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        activity?.startActivity(settingIntent)
    }

    private fun observeWeatherResponse() {
        viewModel.weather.observe(viewLifecycleOwner) { response ->
            Log.d(TAG, "observeWeatherResponse: ${response.data}")
            when (response) {
                is NetworkResult.Success -> {
                    hideShimmer()
                    response.data?.let {
                        hourlyAdapter.submitList(it.hourly)
                        dailyAdapter.submitList(it.daily)
                        initUi(it)

                        sharedPreferences.addString(Constants.LAT, it.lat.toString())
                        sharedPreferences.addString(Constants.LONG, it.lon.toString())
                    }
                }
                is NetworkResult.Error -> {
                    hideShimmer()
                    hideNoConnectionViews()
                    hideNoConnectionViews()
                    Toast.makeText(requireContext(), "error", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Loading -> {
                    showShimmer()
                }
            }
        }
    }

    private fun initUi(weatherResponse: WeatherResponse) {
        binding.apply {

            weatherResponse.current?.let {
                it.dt?.let { date ->
                    tvDate.text = Constants.convertLongToDayDate(
                        date,
                        sharedPreferences.getString(Constants.LANGUAGE, "en")
                    )
                }

                it.weather?.get(0)?.let { weather ->
                    tvWeatherDesc.text = weather.description

                    Glide
                        .with(binding.root)
                        .load("https://openweathermap.org/img/wn/${weather.icon}@2x.png")
                        .into(ivWeather)

                }
            }
            tvTemp.text =
                "${weatherResponse.current?.temp?.toInt()} ${getTemperatureUnit(requireContext())}"
            tvPressureDeg.text =
                "${weatherResponse.current?.pressure} ${requireContext().getString(R.string.hpa)}"
            tvWindDeg.text =
                "${weatherResponse.current?.wind_speed} ${getSpeedUnit(requireContext())}"
            tvHumidityDeg.text = "${weatherResponse.current?.humidity} %"
            tvCloudDeg.text = "${weatherResponse.current?.clouds} %"
            tvRayDeg.text = weatherResponse.current?.uvi.toString()
            tvVisibilityDeg.text = weatherResponse.current?.visibility.toString()
            binding.tvAddress.text = Constants.getAddress(
                requireContext(),
                weatherResponse.lat ?: 0.0,
                weatherResponse.lon ?: 0.0,
                sharedPreferences.getString(Constants.LANGUAGE, "en")
            )
        }
    }

    private fun setupHourlyRecyclerView() {
        binding.rvHourly.apply {
            adapter = hourlyAdapter
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupDailyRecyclerView() {
        binding.rvDaily.apply {
            adapter = dailyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun showShimmer() {
        binding.apply {
            tvAddress.visibility = View.GONE
            tvDate.visibility = View.GONE
            llWeatherCard.visibility = View.GONE
            rvHourly.visibility = View.GONE
            rvDaily.visibility = View.GONE
            shimmerView.visibility = View.VISIBLE

            shimmerView.startShimmer()
        }
    }

    private fun hideShimmer() {
        binding.apply {
            shimmerView.stopShimmer()
            shimmerView.visibility = View.GONE
            llWeatherCard.visibility = View.VISIBLE
            tvAddress.visibility = View.VISIBLE
            tvDate.visibility = View.VISIBLE
            rvHourly.visibility = View.VISIBLE
            rvDaily.visibility = View.VISIBLE
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

