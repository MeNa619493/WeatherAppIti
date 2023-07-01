package com.example.weatherapp.ui.favorites.allfavorites

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.databinding.FragmentFavoritesBinding
import com.example.weatherapp.model.pojo.WeatherResponse
import com.example.weatherapp.ui.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesFragment : Fragment() {
    private lateinit var viewModel: SharedViewModel

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val favoritesAdapter by lazy {
        FavoritesAdapter(object : FavoritesAdapter.WeatherResponseClickListener {
            override fun onItemClicked(weatherResponse: WeatherResponse) {
                findNavController().navigate(
                    FavoritesFragmentDirections.actionFavoritesFragmentToFavoriteDetailsFragment(
                        weatherResponse
                    )
                )
            }

            override fun onDeleteItemClicked(weatherResponse: WeatherResponse) {
                showAlertDialog(weatherResponse)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

    private fun showAlertDialog(weatherResponse: WeatherResponse) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete this item ?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            viewModel.deleteFav(weatherResponse)
        }
        builder.setNegativeButton("Cancel") { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}