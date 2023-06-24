package com.example.weatherapp.ui.home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavAction
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.ui.SharedViewModel
import com.example.weatherapp.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel
    private lateinit var geoCoder: Geocoder

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        geoCoder = Geocoder(requireContext())

        if (viewModel.locationLiveData.value == null) {
            getLastLocation()
        }


        viewModel.locationLiveData.observe(viewLifecycleOwner) {
            Log.d(TAG, "onViewCreated: location is here")
            showAddress(it)
        }
    }

    private fun getLastLocation() {
        if (checkPermission()){
            if (isLocationEnabled()){
                viewModel.requestNewLocationData()
                if (viewModel.getIsMapBoolean(Constants.IS_MAP, false)) {
                    Log.d(TAG, "onViewCreated: Map is here")
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToMapFragment())
                }
                Log.d(TAG, "onViewCreated: GPS is here")
            } else {
                enableLocationSetting()
            }
        } else {
            requestPermission()
        }
    }

    private fun checkPermission() = ActivityCompat.checkSelfPermission(
        requireActivity(),
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED


    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_ID){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastLocation()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun enableLocationSetting() {
        Toast.makeText(requireActivity(), "Turn on Location", Toast.LENGTH_SHORT).show()
        val settingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        activity?.startActivity(settingIntent)
    }

    private fun showAddress(location: Location) {
        try {
            val addressList = geoCoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )
            addressList?.let {
                binding.tvAddress.text = it[0].adminArea
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "HomeFragment"
        private const val PERMISSION_ID = 44
    }
}