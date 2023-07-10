package com.example.weatherapp.ui.splash

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import com.example.weatherapp.ui.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.model.local.HelperSharedPreferences
import com.example.weatherapp.ui.dialog.ChooseDialogFragment
import com.example.weatherapp.ui.home.HomeFragment
import com.example.weatherapp.utils.Constants
import com.example.weatherapp.utils.Constants.setLocale
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPreferences: HelperSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            if (isFirstTime()) {
                if (!supportFragmentManager.isDestroyed) {
                    setIsFirstTime()
                    ChooseDialogFragment().show(supportFragmentManager, "dialog")
                }
            } else {
                setLocale(getLanguageLocale(), this)
                startMainActivity()
            }
        }, 3000)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    private fun getLanguageLocale(): String {
        return sharedPreferences.getString(Constants.LANGUAGE, "en")
    }

    private fun isFirstTime(): Boolean {
        return sharedPreferences.getBoolean(Constants.FIRST_TIME, true)
    }

    private fun setIsFirstTime() {
        sharedPreferences.addBoolean(Constants.FIRST_TIME, false)
    }
}