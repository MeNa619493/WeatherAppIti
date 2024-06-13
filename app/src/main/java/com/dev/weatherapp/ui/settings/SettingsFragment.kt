package com.dev.weatherapp.ui.settings

import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dev.weatherapp.ui.MainActivity
import com.dev.weatherapp.R
import com.dev.weatherapp.databinding.FragmentSettingsBinding
import com.dev.weatherapp.model.data.local.HelperSharedPreferences
import com.dev.weatherapp.utils.Utils
import com.dev.weatherapp.utils.Utils.setLocale
import com.dev.weatherapp.utils.NetworkListener
import com.dev.weatherapp.utils.SnackbarUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(sharedPreferences.getString(Utils.LANGUAGE, "en"), requireContext())
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

            if (selectedLocationSetting != oldLocationSetting || selectedLocationSetting) {
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
                SnackbarUtils.hideSnackbar()
            } else {
                SnackbarUtils.showSnackbar(
                    binding.root,
                    getString(R.string.no_connection),
                    Color.RED
                )
            }
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
            oldUnitSetting = getString(Utils.UNIT, "metric")
            oldLanguageSetting = getString(Utils.LANGUAGE, "en")
            oldLocationSetting = getBoolean(Utils.IS_MAP, false)
        }
    }

    private fun saveSettingsToSharedPreferences() {
        sharedPreferences.apply {
            addString(Utils.UNIT, selectedUnitSetting)
            addString(Utils.LANGUAGE, selectedLanguageSetting)
            addBoolean(Utils.IS_MAP, selectedLocationSetting)
        }
    }


    private fun changeLocationData() {
        sharedPreferences.apply {
            addString(Utils.LAT, "")
            addString(Utils.LONG, "")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}