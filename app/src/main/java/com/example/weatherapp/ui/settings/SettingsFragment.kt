package com.example.weatherapp.ui.settings

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentSettingsBinding
import com.example.weatherapp.model.local.HelperSharedPreferences
import com.example.weatherapp.utils.Constants
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var sharedPreferences: HelperSharedPreferences

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var selectedUnitSetting: String
    private lateinit var selectedLanguageSetting: String
    private var selectedLocationSetting = false

    private lateinit var oldUnitSetting: String
    private lateinit var oldLanguageSetting: String
    private var oldLocationSetting = false

    private lateinit var nav: BottomNavigationView

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

        initializeSettings()

        binding.btnSave.setOnClickListener {
            getLocationSettings()
            getLanguagesSettings()
            getUnitSettings()
            saveSettingsToSharedPreferences()

            setLocale(selectedLanguageSetting)
            nav.selectedItemId = R.id.homeFragment
        }

    }

    private fun setLocale(lang: String) {
        val myLocale = Locale(lang)
        Locale.setDefault(myLocale)
        val res: Resources = resources
        val dm: DisplayMetrics = res.displayMetrics
        val conf: Configuration = res.configuration
        conf.locale = myLocale
        conf.setLayoutDirection(myLocale)
        res.updateConfiguration(conf, dm)
    }

    private fun getUnitSettings() {
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

    private fun saveSettingsToSharedPreferences() {
        sharedPreferences.apply {
            addString(Constants.UNIT, selectedUnitSetting)
            addString(Constants.LANGUAGE, selectedLanguageSetting)
            addBoolean(Constants.IS_MAP, selectedLocationSetting)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}