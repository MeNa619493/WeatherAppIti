package com.example.weatherapp.ui.favorites

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentFavoritesBinding
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.model.pojo.WeatherResponse
import com.example.weatherapp.ui.SharedViewModel
import com.example.weatherapp.ui.home.adapters.HourlyAdapter

class FavoritesFragment : Fragment() {
    private lateinit var viewModel: SharedViewModel

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val favoritesAdapter by lazy {
        FavoritesAdapter(object : FavoritesAdapter.WeatherResponseClickListener {
            override fun onItemClicked(weatherResponse: WeatherResponse) {

            }

            override fun onDeleteItemClicked(weatherResponse: WeatherResponse) {

            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        observeAddButton()
        setupDailyRecyclerView()
        observeAllFavs()
    }

    private fun observeAddButton() {
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(
                FavoritesFragmentDirections.actionFavoritesFragmentToMapFragment(
                    true
                )
            )
        }
    }

    private fun observeAllFavs() {
        viewModel.getAllFavs().observe(viewLifecycleOwner) {
            favoritesAdapter.submitList(it)
        }
    }

    private fun setupDailyRecyclerView() {
        binding.rvFavs.apply {
            adapter = favoritesAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}