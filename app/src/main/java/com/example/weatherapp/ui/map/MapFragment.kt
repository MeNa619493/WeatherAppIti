package com.example.weatherapp.ui.map

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.databinding.FragmentMapBinding
import com.example.weatherapp.ui.SharedViewModel
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

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SharedViewModel
    private lateinit var map: GoogleMap
    private lateinit var selectedLocation: Location

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        viewModel.locationLiveData.observe(viewLifecycleOwner) {
            setUserLocation(it.latitude, it.longitude)
        }

        binding.btnDone.setOnClickListener {
            onLocationSelected()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        onMapClicked()
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
        autocompleteFragment?.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

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

    private fun onLocationSelected() {
        if (this::selectedLocation.isInitialized) {
            viewModel.locationLiveData.value = selectedLocation
        }
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "MapFragment"
    }

}