package com.example.weatherapp.ui.alerts.dialog

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentAlertDialogBinding
import com.example.weatherapp.model.local.HelperSharedPreferences
import com.example.weatherapp.model.pojo.WeatherAlert
import com.example.weatherapp.ui.alerts.SharedAlertViewModel
import com.example.weatherapp.utils.Constants
import com.example.weatherapp.utils.Constants.convertLongToTimePicker
import com.example.weatherapp.utils.Constants.getDateMillis
import com.example.weatherapp.utils.Constants.setLocale
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class AlertDialogFragment : DialogFragment() {

    private var _binding: FragmentAlertDialogBinding? = null
    private val binding get() = _binding!!

    private var timeFrom: Long = 0
    private var timeTo: Long = 0
    private var startDate: Long = 0
    private var endDate: Long = 0

    private lateinit var viewModel: SharedAlertViewModel

    @Inject
    lateinit var sharedPreferences: HelperSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(getLanguageLocale(), requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.round_corner);
        _binding = FragmentAlertDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[SharedAlertViewModel::class.java]

        binding.dateFrom.setOnClickListener {
            showDatePicker(true)
        }

        binding.dateTo.setOnClickListener {
            showDatePicker(false)
        }

        binding.hourFrom.setOnClickListener {
            showTimePicker(true)
        }

        binding.hourTo.setOnClickListener {
            showTimePicker(false)
        }

        binding.btnSave.setOnClickListener {
            val alert = WeatherAlert(
                startDate = startDate,
                endDate = endDate,
                timeFrom = timeFrom,
                timeTo = timeTo
            )
            if (startDate != 0L && endDate != 0L && timeFrom != 0L && timeTo != 0L) {
                viewModel.saveWeatherAlert(alert)
                Log.e("AlertDialogFragment", "save clicked")
            } else {
                Toast.makeText(requireContext(), "Please, Enter valid date", Toast.LENGTH_SHORT)
                    .show()
            }
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog!!.setCanceledOnTouchOutside(true)
        dialog!!.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showDatePicker(isFrom: Boolean) {
        val myCalender = Calendar.getInstance()
        val year = myCalender[Calendar.YEAR]
        val month = myCalender[Calendar.MONTH]
        val day = myCalender[Calendar.DAY_OF_MONTH]
        val myDateListener =
            DatePickerDialog.OnDateSetListener { view, year, month, day ->
                if (view.isShown) {
                    val date = "$day/${month + 1}/$year"
                    if (isFrom) {
                        binding.dateFrom.text = date
                        startDate = getDateMillis(date, getLanguageLocale())
                    } else {
                        binding.dateTo.text = date
                        endDate = getDateMillis(date, getLanguageLocale())
                    }
                }
            }
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            myDateListener,
            year,
            month,
            day
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun showTimePicker(isFrom: Boolean) {
        val myCalender = Calendar.getInstance()
        val currentHour = myCalender.get(Calendar.HOUR_OF_DAY)
        val currentMinute = myCalender.get(Calendar.MINUTE)
        val listener: (TimePicker?, Int, Int) -> Unit = { view, hour: Int, minute: Int ->
            view?.let {
                if (it.isShown) {
                    val time =
                        (TimeUnit.MINUTES.toMillis(minute.toLong()) + TimeUnit.HOURS.toMillis(hour.toLong()))
                    if (isFrom) {
                        binding.hourFrom.text = convertLongToTimePicker(time, getLanguageLocale())
                        timeFrom = time
                    } else {
                        binding.hourTo.text = convertLongToTimePicker(time, getLanguageLocale())
                        timeTo = time
                    }
                }
            }
        }

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            listener,
            currentHour,
            currentMinute,
            false
        )
        timePickerDialog.show()
    }

    private fun getLanguageLocale(): String {
        return sharedPreferences.getString(Constants.LANGUAGE, "en")
    }

}