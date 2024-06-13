package com.dev.weatherapp.utils

import android.graphics.Color
import android.view.View
import com.google.android.material.snackbar.Snackbar


class SnackbarUtils {
    companion object {
        private var snackbar: Snackbar? = null

        fun showSnackbar(view: View, message: String, backgroundColor: Int) {
            snackbar?.dismiss()
            snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            val snackbarView = snackbar?.view
            snackbarView?.setBackgroundColor(backgroundColor)
            snackbar?.setTextColor(Color.WHITE)
            snackbar?.show()
        }

        fun hideSnackbar() {
            snackbar?.dismiss()
        }
    }
}