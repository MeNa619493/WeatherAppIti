package com.example.weatherapp.ui.favorites.favoritedetails

import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentFavoriteDetailsBinding
import com.example.weatherapp.model.data.local.HelperSharedPreferences
import com.example.weatherapp.model.pojo.WeatherResponse
import com.example.weatherapp.ui.home.adapters.DailyAdapter
import com.example.weatherapp.ui.home.adapters.HourlyAdapter
import com.example.weatherapp.utils.Utils
import com.example.weatherapp.utils.Utils.getSpeedUnit
import com.example.weatherapp.utils.Utils.getTemperatureUnit
import com.example.weatherapp.utils.NetworkListener
import com.example.weatherapp.utils.NetworkResult
import com.example.weatherapp.utils.SnackbarUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FavoriteDetailsFragment : Fragment() {

    private var _binding: FragmentFavoriteDetailsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPreferences: HelperSharedPreferences

    @Inject
    lateinit var networkChangeListener: NetworkListener

    private val hourlyAdapter by lazy { HourlyAdapter() }
    private val dailyAdapter by lazy { DailyAdapter() }

    val viewModel: FavoriteDetailsViewModel by viewModels()
    private val args: FavoriteDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoriteDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        ContextCompat.registerReceiver(
            requireActivity(),
            networkChangeListener,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        observeNetworkState()
        observeWeatherResponse()
        setupHourlyRecyclerView()
        setupDailyRecyclerView()
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
                SnackbarUtils.hideSnackbar()
                viewModel.getFavWeather(
                    args.weather.lat.toString(),
                    args.weather.lon.toString(),
                    getUnits(),
                    getLanguageLocale()
                )
            } else {
                showShimmer()
                hourlyAdapter.submitList(args.weather.hourly)
                dailyAdapter.submitList(args.weather.daily)
                initUi(args.weather)
                SnackbarUtils.showSnackbar(
                    binding.root,
                    getString(R.string.no_connection),
                    Color.RED
                )
            }
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
                    hideShimmer()
                }
                is NetworkResult.Loading -> {
                    showShimmer()
                }
            }
        }
    }

    private fun initUi(weatherResponse: WeatherResponse) {
        hideShimmer()

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
                ""
            )
            tvRayDeg.text = uvFormat

            val visibilityFormat = getString(
                R.string.visibility_deg,
                weatherResponse.current?.visibility,
                ""
            )
            tvVisibilityDeg.text = visibilityFormat

            binding.tvAddress.text = Utils.getAddress(
                requireContext(),
                weatherResponse.lat ?: 0.0,
                weatherResponse.lon ?: 0.0,
            )
        }
    }

    private fun setupHourlyRecyclerView() {
        binding.rvHourly.apply {
            adapter = hourlyAdapter
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "FavoriteDetailsFragment"
    }

}