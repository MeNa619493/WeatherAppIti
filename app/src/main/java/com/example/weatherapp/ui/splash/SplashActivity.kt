package com.example.weatherapp.ui.splash

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import com.example.weatherapp.ui.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.model.local.HelperSharedPreferences
import com.example.weatherapp.ui.dialog.ChooseDialogFragment
import com.example.weatherapp.utils.Constants
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
            if (isFirstTime()){
                if (!supportFragmentManager.isDestroyed) {
                    setIsFirstTime()
                    ChooseDialogFragment().show(supportFragmentManager, "dialog")
                }
            } else {
                setLocale(sharedPreferences.getString(Constants.LANGUAGE, "en"))
                startMainActivity()
            }
        }, 3000)
    }

    private fun setLocale(language: String) {
        val myLocale = Locale(language)
        Locale.setDefault(myLocale)
        val res: Resources = resources
        val dm: DisplayMetrics = res.displayMetrics
        val conf: Configuration = res.configuration
        conf.locale = myLocale
        conf.setLayoutDirection(myLocale)
        res.updateConfiguration(conf, dm)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun isFirstTime(): Boolean {
        return sharedPreferences.getBoolean(Constants.FIRST_TIME, true)
    }

    private fun setIsFirstTime() {
        sharedPreferences.addBoolean(Constants.FIRST_TIME, false)
    }
}