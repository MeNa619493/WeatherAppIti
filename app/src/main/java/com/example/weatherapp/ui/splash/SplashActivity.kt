package com.example.weatherapp.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.activity.viewModels
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.ui.dialog.ChooseDialogFragment
import com.example.weatherapp.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            if (viewModel.getFirstTimeBoolean(Constants.FIRST_TIME, true)){
                if (!supportFragmentManager.isDestroyed) {
                    ChooseDialogFragment().show(supportFragmentManager, "First time settings")
                }
            } else {
                startMainActivity()
            }
        }, 3000)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}