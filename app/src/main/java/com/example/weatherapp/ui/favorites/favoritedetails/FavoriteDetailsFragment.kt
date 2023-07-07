package com.example.weatherapp.ui.favorites.favoritedetails

import android.content.IntentFilter
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
import com.example.weatherapp.databinding.FragmentFavoritesBinding
import com.example.weatherapp.model.local.HelperSharedPreferences
import com.example.weatherapp.model.pojo.WeatherResponse
import com.example.weatherapp.ui.home.adapters.DailyAdapter
import com.example.weatherapp.ui.home.adapters.HourlyAdapter
import com.example.weatherapp.utils.Constants
import com.example.weatherapp.utils.Constants.getSpeedUnit
import com.example.weatherapp.utils.Constants.getTemperatureUnit
import com.example.weatherapp.utils.NetworkListener
import com.example.weatherapp.utils.NetworkResult
import com.google.android.material.snackbar.Snackbar
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

    private val hourlyAdapter by lazy { HourlyAdapter(requireContext()) }
    private val dailyAdapter by lazy { DailyAdapter(requireContext()) }

    val viewModel: FavoriteDetailsViewModel by viewModels()
    private val args: FavoriteDetailsFragmentArgs by navArgs()

    private lateinit var snackbar: Snackbar

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
                hideSnackbar()
                viewModel.getFavWeather(
                    args.weather.lat.toString(),
                    args.weather.lon.toString(),
                    sharedPreferences.getString(Constants.UNIT, "metric"),
                    sharedPreferences.getString(Constants.LANGUAGE, "en")
                )
            } else {
                showShimmer()
                hourlyAdapter.submitList(args.weather.hourly)
                dailyAdapter.submitList(args.weather.daily)
                initUi(args.weather)
                showSnackbar()
            }
        }
    }

    private fun showSnackbar() {
        val rootView = activity?.findViewById<View>(android.R.id.content)
        snackbar =
            Snackbar.make(rootView!!, getString(R.string.no_connection), Snackbar.LENGTH_INDEFINITE)
        val layoutParams = snackbar.view.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.bottomMargin =
            resources.getDimensionPixelSize(R.dimen.bottom_navigation_height)
        snackbar.view.layoutParams = layoutParams
        snackbar.setActionTextColor(resources.getColor(android.R.color.white))
        snackbar.view.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
        snackbar.show()
    }

    private fun hideSnackbar() {
        if (this::snackbar.isInitialized) {
            snackbar.dismiss()
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
            tvAddress.text = Constants.getAddress(
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
        private const val TAG = "FavoriteDetailsFragment"
    }

}