package com.example.weatherapp.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.weatherapp.ui.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.model.data.local.HelperSharedPreferences
import com.example.weatherapp.ui.dialog.ChooseDialogFragment
import com.example.weatherapp.utils.Utils
import com.example.weatherapp.utils.Utils.setLocale
import dagger.hilt.android.AndroidEntryPoint
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
        return sharedPreferences.getString(Utils.LANGUAGE, "en")
    }

    private fun isFirstTime(): Boolean {
        return sharedPreferences.getBoolean(Utils.FIRST_TIME, true)
    }

    private fun setIsFirstTime() {
        sharedPreferences.addBoolean(Utils.FIRST_TIME, false)
    }
}