package com.example.weatherapp.ui.map

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentMapBinding
import com.example.weatherapp.model.data.local.HelperSharedPreferences
import com.example.weatherapp.model.pojo.UserLocation
import com.example.weatherapp.ui.SharedViewModel
import com.example.weatherapp.utils.Utils
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPreferences: HelperSharedPreferences

    private lateinit var viewModel: SharedViewModel
    private lateinit var map: GoogleMap
    private lateinit var selectedLocation: Location
    private val args: MapFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        addAutoComplete()
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        binding.btnDone.setOnClickListener {
            onLocationSelected()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        observeUserCurrentLocation()
        onMapClicked()
    }

    private fun observeUserCurrentLocation() {
        viewModel.locationLiveData.observe(viewLifecycleOwner) {
            setUserLocation(it.lat, it.lon)
        }
    }

    private fun setUserLocation(latitude: Double, longitude: Double) {
        val location = LatLng(latitude, longitude)
        map.addMarker(
            MarkerOptions()
                .position(location)
                .title("Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10.0f))
        map.uiSettings.isZoomControlsEnabled = true
    }

    private fun onMapClicked() {
        map.setOnMapClickListener { location ->
            map.clear()
            map.addMarker(MarkerOptions().position(location))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10.0f))
            selectedLocation = Location("")
            selectedLocation.longitude = location.longitude
            selectedLocation.latitude = location.latitude
            binding.btnDone.visibility = View.VISIBLE
        }
    }

    private fun onLocationSelected() {
        if (this::selectedLocation.isInitialized) {
            val location = UserLocation(selectedLocation.latitude, selectedLocation.longitude)
            if (args.isFav) {
                saveFavLocation(location)
            } else {
                sharedPreferences.addString(Utils.LAT, location.lat.toString())
                sharedPreferences.addString(Utils.LONG, location.lon.toString())
                viewModel.locationLiveData.value = location
            }
        }
        findNavController().popBackStack()
    }

    private fun saveFavLocation(location: UserLocation) {
        viewModel.saveFavLocationWeather(
            "${location.lat}",
            "${location.lon}",
            getUnits(),
            getLanguageLocale(),
            )
    }

    private fun addAutoComplete() {
        val api = getString(R.string.api_key)

        if (!Places.isInitialized()) {
            Places.initialize(requireContext().applicationContext, api)
        }
        val placesClient = Places.createClient(requireContext())
        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            activity?.supportFragmentManager?.findFragmentById(R.id.autocomplete_fragment)
                    as? AutocompleteSupportFragment

        autocompleteFragment?.setTypeFilter(TypeFilter.ADDRESS)

        autocompleteFragment?.setLocationBias(
            RectangularBounds.newInstance(
                LatLng(-33.880490, 151.184363),
                LatLng(-33.858754, 151.229596)
            )
        )

        // Specify the types of place data to return.
        autocompleteFragment?.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: ${place.name}, ${place.id}")
                place.latLng?.let {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 10.0f))
                }
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })
    }

    private fun getLanguageLocale(): String {
        return sharedPreferences.getString(Utils.LANGUAGE, "en")
    }

    private fun getUnits(): String {
        return sharedPreferences.getString(Utils.UNIT, "metric")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "MapFragment"
    }

}