package com.example.weatherapp.ui.home

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import androidx.core.content.ContextCompat.registerReceiver
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.model.data.local.HelperSharedPreferences
import com.example.weatherapp.model.pojo.UserLocation
import com.example.weatherapp.model.pojo.WeatherResponse
import com.example.weatherapp.ui.SharedViewModel
import com.example.weatherapp.ui.home.adapters.DailyAdapter
import com.example.weatherapp.ui.home.adapters.HourlyAdapter
import com.example.weatherapp.utils.Utils
import com.example.weatherapp.utils.Utils.getSpeedUnit
import com.example.weatherapp.utils.Utils.getTemperatureUnit
import com.example.weatherapp.utils.Utils.setLocale
import com.example.weatherapp.utils.NetworkListener
import com.example.weatherapp.utils.NetworkResult
import com.example.weatherapp.utils.SnackbarUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPreferences: HelperSharedPreferences

    private val hourlyAdapter by lazy { HourlyAdapter() }
    private val dailyAdapter by lazy { DailyAdapter() }

    @Inject
    lateinit var networkChangeListener: NetworkListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(getLanguageLocale(), requireContext())
    }

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
                if (getLat().isBlank() || getLong().isBlank()) {
                    getLastLocation()
                } else {
                    val location = UserLocation(getLat().toDouble(), getLong().toDouble())
                    viewModel.locationLiveData.value = location
                }
            } else{
                SnackbarUtils.showSnackbar(
                    binding.root,
                    getString(R.string.no_connection),
                    Color.RED
                )
                viewModel.getCachedWeather()
            }
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
                getUnits(),
                getLanguageLocale()
            )
        }
    }

    private fun getLastLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                viewModel.requestNewLocationData()
                if (sharedPreferences.getBoolean(Utils.IS_MAP, false)) {
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
        requestPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ).toString()
        )
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(TAG, "onRequestPermissionsResult: PERMISSION_GRANTED")
                getLastLocation()
            } else {
                Log.d(TAG, "onRequestPermissionsResult: PERMISSION_DENIED")
                hideAllViews()
                showAlertDialog()
            }
        }

    private fun showAlertDialog() {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Alert")
        alertDialogBuilder.setMessage(getString(R.string.ask_permission))
        alertDialogBuilder.setPositiveButton(getString(R.string.permission_postive_button)) { dialog: DialogInterface, _: Int ->
            gotoAppPermission()
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        alertDialogBuilder.setCancelable(false)
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun gotoAppPermission() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", activity?.packageName, null)
        intent.data = uri
        startActivity(intent)
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

                        sharedPreferences.addString(Utils.LAT, it.lat.toString())
                        sharedPreferences.addString(Utils.LONG, it.lon.toString())
                    }
                }
                is NetworkResult.Error -> {
                    hideShimmer()
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
                    tvDate.text = Utils.convertLongToDayDate(date)
                }

                it.weather?.get(0)?.let { weather ->
                    tvWeatherDesc.text = weather.description

                    Glide
                        .with(binding.root)
                        .load("https://openweathermap.org/img/wn/${weather.icon}@2x.png")
                        .into(ivWeather)

                }
            }

            val tempFormat = getString(
                R.string.temp_deg,
                weatherResponse.current?.temp?.toInt(),
                getTemperatureUnit(requireContext())
            )
            tvTemp.text = tempFormat

            val pressureFormat = getString(
                R.string.pressure_deg,
                weatherResponse.current?.pressure,
                requireContext().getString(R.string.hpa)
            )
            tvPressureDeg.text = pressureFormat

            val windFormat: String = getString(
                R.string.wind_deg,
                weatherResponse.current?.wind_speed?.toInt(),
                getSpeedUnit(requireContext())
            )
            tvWindDeg.text = windFormat

            val humidityFormat = getString(
                R.string.humidity_deg,
                weatherResponse.current?.humidity,
                "%"
            )
            tvHumidityDeg.text = humidityFormat

            val cloudFormat = getString(
                R.string.cloud_deg,
                weatherResponse.current?.clouds,
                "%"
            )
            tvCloudDeg.text = cloudFormat

            val uvFormat = getString(
                R.string.uv_deg,
                weatherResponse.current?.uvi?.toInt(),
                " "
            )
            tvRayDeg.text = uvFormat

            val visibilityFormat = getString(
                R.string.visibility_deg,
                weatherResponse.current?.visibility,
                " "
            )
            tvVisibilityDeg.text = visibilityFormat

            binding.tvAddress.text = Utils.getAddress(
                requireContext(),
                weatherResponse.lat ?: 0.0,
                weatherResponse.lon ?: 0.0
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

    private fun getLanguageLocale(): String {
        return sharedPreferences.getString(Utils.LANGUAGE, "en")
    }

    private fun getUnits(): String {
        return sharedPreferences.getString(Utils.UNIT, "metric")
    }

    private fun getLat(): String {
        return sharedPreferences.getString(Utils.LAT, "")
    }

    private fun getLong(): String {
        return sharedPreferences.getString(Utils.LONG, "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}

