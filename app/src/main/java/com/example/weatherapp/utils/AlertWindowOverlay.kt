package com.example.weatherapp.utils

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.*
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.databinding.PopupAlertBinding

class AlertWindowOverlay(
    private val context: Context,
    private val description: String,
    private val icon: String
) {
    private lateinit var windowManager: WindowManager
    private lateinit var dialogView: View
    private lateinit var binding: PopupAlertBinding

    fun setAlertWindowManger() {
        Log.e("setAlertWindowManger", "AlertWindowOverlay")
        val layoutParamsType: Int =
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        val params = WindowManager.LayoutParams(
            (context.resources.displayMetrics.widthPixels * 0.90).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutParamsType,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE,
            PixelFormat.TRANSLUCENT
        )

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        dialogView = inflater.inflate(R.layout.popup_alert, null)
        binding = PopupAlertBinding.bind(dialogView)

        initializeUI()

        windowManager.addView(dialogView, params)
    }

    private fun initializeUI() {
        binding.apply {
            tvDesc.text = description

            Glide
                .with(binding.root)
                .load("https://openweathermap.org/img/wn/${icon}@2x.png")
                .into(ivWeatherDesc)

            btnDismiss.setOnClickListener {
                removeDialog()
            }
        }
    }

    private fun removeDialog() {
        if (this::dialogView.isInitialized) {
            windowManager.removeView(dialogView)
        }
    }
}