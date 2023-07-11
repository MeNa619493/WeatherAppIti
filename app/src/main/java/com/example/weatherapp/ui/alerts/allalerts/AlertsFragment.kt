package com.example.weatherapp.ui.alerts.allalerts

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentAlertsBinding
import com.example.weatherapp.model.data.local.HelperSharedPreferences
import com.example.weatherapp.model.pojo.WeatherAlert
import com.example.weatherapp.ui.alerts.SharedAlertViewModel
import com.example.weatherapp.utils.Utils
import com.example.weatherapp.utils.Utils.setLocale
import com.example.weatherapp.utils.NetworkListener
import com.example.weatherapp.utils.SnackbarUtils
import com.example.weatherapp.workmanager.DailyWorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class AlertsFragment : Fragment() {

    private lateinit var viewModel: SharedAlertViewModel

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var networkChangeListener: NetworkListener

    @Inject
    lateinit var sharedPreferences: HelperSharedPreferences

    private val alertsAdapter by lazy {
        AlertsAdapter(
            object : AlertsAdapter.AlertClickListener {
                override fun onDeleteItemClicked(weatherAlert: WeatherAlert) {
                    showAlertDialog(weatherAlert)
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
        // Inflate the layout for this fragment
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[SharedAlertViewModel::class.java]

        setupAlertsRecyclerView()
        observeNetworkState()

        binding.floatingActionButton.setOnClickListener {
            showDialog()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.alertInsertedSuccess.collect {
                if (it) {
                    setDailyWorkManger()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.deletedAlertId.collect { id ->
                Log.e("AlertDialogFragment", "observe delete alertId = $id")
                WorkManager.getInstance(requireContext()).cancelUniqueWork("$id")
            }
        }
    }

    override fun onResume() {
        super.onResume()

        observeAllAlerts()
        deleteAlerts()
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

    private fun deleteAlerts() {
        viewModel.deleteAlerts()
    }

    private fun observeAllAlerts() {
        viewModel.getAllAlerts().observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.apply {
                    rvAlerts.visibility = View.GONE
                    ivNoData.visibility = View.VISIBLE
                    tvNoData.visibility = View.VISIBLE
                }
            } else {
                binding.apply {
                    rvAlerts.visibility = View.VISIBLE
                    ivNoData.visibility = View.GONE
                    tvNoData.visibility = View.GONE
                }
                alertsAdapter.submitList(it)
            }
        }
    }

    private fun showAlertDialog(weatherAlert: WeatherAlert) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete this item ?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { _, _ ->
            viewModel.deleteAlert(weatherAlert)
        }
        builder.setNegativeButton("Cancel") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun navigateToSetTimeDialog() {
        findNavController().navigate(AlertsFragmentDirections.actionAlertsFragmentToAlertDialogFragment())
    }

    private fun showDialog() {
        if (checkPermission()) {
            navigateToSetTimeDialog()
        } else {
            requestPermission()
        }
    }

    private fun checkPermission() = Settings.canDrawOverlays(requireContext())

    private fun requestPermission() {
        val alertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
        alertDialogBuilder.setTitle("Alert")
            .setMessage("Need Your Permission to set alarms")
            .setPositiveButton("Yes") { dialog: DialogInterface, _: Int ->
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + requireActivity().packageName)
                )
                // request permission via start activity for result
                startActivityForResult(intent, PERMISSION_ID)
                //It will call onActivityResult Function After you press Yes/No and go Back after giving permission
                dialog.dismiss()

            }.setNegativeButton(
                "Cancel"
            ) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }.show()
    }

    private fun setupAlertsRecyclerView() {
        binding.rvAlerts.apply {
            adapter = alertsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_ID) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                navigateToSetTimeDialog()
            }
        }
    }

    private fun setDailyWorkManger() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val periodicWorkRequest = PeriodicWorkRequest.Builder(
            DailyWorkManager::class.java, 24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "daily",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest
        )

        Log.e("setDailyWorkManger", "periodicWorkRequest done")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getLanguageLocale(): String {
        return sharedPreferences.getString(Utils.LANGUAGE, "en")
    }

    companion object {
        private const val PERMISSION_ID = 55
    }

}