package com.example.weatherapp.ui.favorites.allfavorites

import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentFavoritesBinding
import com.example.weatherapp.model.data.local.HelperSharedPreferences
import com.example.weatherapp.model.pojo.WeatherResponse
import com.example.weatherapp.ui.SharedViewModel
import com.example.weatherapp.utils.Utils
import com.example.weatherapp.utils.Utils.setLocale
import com.example.weatherapp.utils.NetworkListener
import com.example.weatherapp.utils.SnackbarUtils
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FavoritesFragment : Fragment() {
    private lateinit var viewModel: SharedViewModel

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    lateinit var favList: MutableList<WeatherResponse>

    @Inject
    lateinit var sharedPreferences: HelperSharedPreferences

    @Inject
    lateinit var networkChangeListener: NetworkListener

    private val favoritesAdapter by lazy {
        FavoritesAdapter(
            requireContext(),
            object : FavoritesAdapter.WeatherResponseClickListener {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(getLanguageLocale(), requireContext())
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

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        ContextCompat.registerReceiver(
            requireActivity(),
            networkChangeListener,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        observeNetworkState()
        setupDailyRecyclerView()
        observeAddButton()
        observeAllFavs()
        swipeToDelete()
    }

    override fun onPause() {
        super.onPause()
        activity?.let {
            LocalBroadcastManager.getInstance(it).unregisterReceiver(networkChangeListener)
        }
    }

    private fun observeNetworkState() {
        NetworkListener.isNetworkAvailable.observe(viewLifecycleOwner) {
            if (it) {
                SnackbarUtils.hideSnackbar()
            } else {
                SnackbarUtils.showSnackbar(
                    binding.root,
                    getString(R.string.no_connection),
                    Color.RED
                )
            }
        }
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
            if (it.isEmpty()) {
                handleViewWhenListEmpty()
            } else {
                handleViewWhenDataRetrieved()
                favList = it.toMutableList()
                favoritesAdapter.submitList(it)
            }
        }
    }

    private fun handleViewWhenListEmpty() {
        binding.apply {
            rvFavs.visibility = View.GONE
            ivNoData.visibility = View.VISIBLE
            tvNoData.visibility = View.VISIBLE
        }
    }

    private fun handleViewWhenDataRetrieved() {
        binding.apply {
            rvFavs.visibility = View.VISIBLE
            ivNoData.visibility = View.GONE
            tvNoData.visibility = View.GONE
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
        builder.setPositiveButton("Yes") { _, _ ->
            viewModel.deleteFav(weatherResponse)
        }
        builder.setNegativeButton("Cancel") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun swipeToDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem: WeatherResponse = favList[viewHolder.adapterPosition]
                viewModel.deleteFav(deletedItem)
                Snackbar.make(binding.rvFavs, "Item Deleted", Snackbar.LENGTH_LONG)
                    .setAction(
                        "Undo",
                        View.OnClickListener {
                            viewModel.addFavLocation(deletedItem)
                        }).show()
            }
        }).attachToRecyclerView(binding.rvFavs)
    }

    private fun getLanguageLocale(): String {
        return sharedPreferences.getString(Utils.LANGUAGE, "en")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}