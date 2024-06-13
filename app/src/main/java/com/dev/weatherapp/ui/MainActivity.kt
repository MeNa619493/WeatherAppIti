package com.dev.weatherapp.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.dev.weatherapp.R
import com.dev.weatherapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        val navController = findNavController(this, R.id.nav_host_fragment)
        setupWithNavController(binding.navigationBar, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if(destination.id == R.id.mapFragment || destination.id == R.id.favoriteDetailsFragment ) {
                binding.navigationBar.visibility = View.GONE
            } else {
                binding.navigationBar.visibility  = View.VISIBLE
            }
        }
    }
}


