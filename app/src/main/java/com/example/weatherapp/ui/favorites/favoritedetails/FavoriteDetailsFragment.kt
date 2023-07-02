package com.example.weatherapp.ui.favorites.favoritedetails

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentFavoriteDetailsBinding
import com.example.weatherapp.databinding.FragmentFavoritesBinding
import com.example.weatherapp.model.local.HelperSharedPreferences
import com.example.weatherapp.model.pojo.WeatherResponse
import com.example.weatherapp.ui.home.adapters.DailyAdapter
import com.example.weatherapp.ui.home.adapters.HourlyAdapter
import com.example.weatherapp.utils.Constants
import com.example.weatherapp.utils.Constants.getTemperatureUnit
import com.example.weatherapp.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FavoriteDetailsFragment : Fragment() {

    private var _binding: FragmentFavoriteDetailsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPreferences: HelperSharedPreferences

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

        viewModel.getFavWeather(
            args.weather.lat.toString(),
            args.weather.lon.toString(),
            sharedPreferences.getString(Constants.UNIT, "metric"),
            sharedPreferences.getString(Constants.LANGUAGE, "en")
        )

        observeWeatherResponse()
        setupHourlyRecyclerView()
        setupDailyRecyclerView()
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

            weatherResponse.current?.let {
                it.dt?.let { date ->
                    tvDate.text = Constants.convertLongToDayDate(date)
                }

                it.weather?.get(0)?.let { weather ->
                    tvWeatherDesc.text = weather.description

                    Glide
                        .with(binding.root)
                        .load("https://openweathermap.org/img/wn/${weather.icon}@2x.png")
                        .into(ivWeather)

                }
            }
            tvTemp.text =  "${weatherResponse.current?.temp?.toInt()} ${getTemperatureUnit(requireContext())}"
            tvPressureDeg.text = weatherResponse.current?.pressure.toString()
            tvWindDeg.text = weatherResponse.current?.wind_speed.toString()
            tvHumidityDeg.text = weatherResponse.current?.humidity.toString()
            tvCloudDeg.text = weatherResponse.current?.clouds.toString()
            tvRayDeg.text = weatherResponse.current?.uvi.toString()
            tvVisibilityDeg.text = weatherResponse.current?.visibility.toString()
            tvAddress.text = args.weather.location
        }
    }

    private fun setupHourlyRecyclerView() {
        binding.rvHourly.apply {
            adapter = hourlyAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupDailyRecyclerView() {
        binding.rvDaily.apply {
            adapter = dailyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "FavoriteDetailsFragment"
    }

}