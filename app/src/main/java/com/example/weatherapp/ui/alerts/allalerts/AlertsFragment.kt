package com.example.weatherapp.ui.alerts.allalerts

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.example.weatherapp.databinding.FragmentAlertsBinding
import com.example.weatherapp.ui.alerts.SharedAlertViewModel
import com.example.weatherapp.utils.Constants
import com.example.weatherapp.workmanager.DailyWorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class AlertsFragment : Fragment() {

    private lateinit var viewModel: SharedAlertViewModel

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private val alertsAdapter by lazy { AlertsAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[SharedAlertViewModel::class.java]

        setupAlertsRecyclerView()

        viewModel.getAllAlerts().observe(viewLifecycleOwner) {
            alertsAdapter.submitList(it)
        }

        binding.floatingActionButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                showDialog()
            } else {
                navigateToSetTimeDialog()
            }
        }

        viewModel.alertId.observe(viewLifecycleOwner) {
            Log.e("AlertDialogFragment", "observe alertId")
            setDailyWorkManger()
        }
    }

    private fun navigateToSetTimeDialog() {
        findNavController().navigate(AlertsFragmentDirections.actionAlertsFragmentToAlertDialogFragment())
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showDialog() {
        if (checkPermission()) {
            navigateToSetTimeDialog()
        } else {
            requestPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission() = Settings.canDrawOverlays(requireContext())


    @RequiresApi(Build.VERSION_CODES.M)
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

        if (requestCode == PERMISSION_ID){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
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
            "0",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest
        )

        Log.e("setDailyWorkManger", "periodicWorkRequest done")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PERMISSION_ID = 55
    }

}