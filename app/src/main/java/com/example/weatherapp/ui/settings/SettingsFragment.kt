package com.example.weatherapp.ui.settings

import android.content.IntentFilter
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.weatherapp.ui.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentSettingsBinding
import com.example.weatherapp.model.local.HelperSharedPreferences
import com.example.weatherapp.utils.Constants
import com.example.weatherapp.utils.Constants.setLocale
import com.example.weatherapp.utils.NetworkListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var sharedPreferences: HelperSharedPreferences

    @Inject
    lateinit var networkChangeListener: NetworkListener

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var selectedUnitSetting: String
    private lateinit var selectedLanguageSetting: String
    private var selectedLocationSetting = false

    private lateinit var oldUnitSetting: String
    private lateinit var oldLanguageSetting: String
    private var oldLocationSetting = false

    private lateinit var nav: BottomNavigationView
    private lateinit var snackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(sharedPreferences.getString(Constants.LANGUAGE, "en"), requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        nav = (activity as MainActivity).findViewById(R.id.navigation_bar)
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

        initializeSettings()
        observeNetworkState()

        binding.btnSave.setOnClickListener {
            getLocationSettings()
            getLanguagesSettings()
            getUnitsSettings()
            saveSettingsToSharedPreferences()

            if (selectedLocationSetting) {
                changeLocationData()
            }

            setLocale(selectedLanguageSetting, requireContext())
            nav.selectedItemId = R.id.homeFragment
        }

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
            } else {
                showSnackbar()
            }
        }
    }

    private fun showSnackbar() {
        val rootView = activity?.findViewById<View>(android.R.id.content)
        snackbar = Snackbar.make(rootView!!, getString(R.string.no_connection), Snackbar.LENGTH_INDEFINITE)
        val layoutParams = snackbar.view.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.bottom_navigation_height)
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

    private fun initializeSettings() {
        getOldSettings()

        when (oldUnitSetting) {
            "metric" -> binding.radioCms.isChecked = true
            "imperial" -> binding.radioFmh.isChecked = true
            "standard" -> binding.radioKms.isChecked = true
        }

        when (oldLanguageSetting) {
            "ar" -> binding.radioArabic.isChecked = true
            "en" -> binding.radioEnglish.isChecked = true
        }

        when (oldLocationSetting) {
            true -> binding.radioSettingMap.isChecked = true
            false -> binding.radioSettingGps.isChecked = true
        }
    }

    private fun getUnitsSettings() {
        when (binding.radioGroupUnits.checkedRadioButtonId) {
            R.id.radio_cms -> selectedUnitSetting = "metric"
            R.id.radio_fmh -> selectedUnitSetting = "imperial"
            R.id.radio_kms -> selectedUnitSetting = "standard"
        }
    }

    private fun getLanguagesSettings() {
        when (binding.radioGroupLang.checkedRadioButtonId) {
            R.id.radio_arabic -> selectedLanguageSetting = "ar"
            R.id.radio_english -> selectedLanguageSetting = "en"
        }
    }

    private fun getLocationSettings() {
        when (binding.radioGroupLoc.checkedRadioButtonId) {
            R.id.radio_setting_map -> selectedLocationSetting = true
            R.id.radio_setting_gps -> selectedLocationSetting = false
        }
    }

    private fun getOldSettings() {
        sharedPreferences.apply {
            oldUnitSetting = getString(Constants.UNIT, "metric")
            oldLanguageSetting = getString(Constants.LANGUAGE, "en")
            oldLocationSetting = getBoolean(Constants.IS_MAP, false)
        }
    }

    private fun saveSettingsToSharedPreferences() {
        sharedPreferences.apply {
            addString(Constants.UNIT, selectedUnitSetting)
            addString(Constants.LANGUAGE, selectedLanguageSetting)
            addBoolean(Constants.IS_MAP, selectedLocationSetting)
        }
    }


    private fun changeLocationData() {
        sharedPreferences.apply {
            addString(Constants.LAT, "")
            addString(Constants.LONG, "")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}