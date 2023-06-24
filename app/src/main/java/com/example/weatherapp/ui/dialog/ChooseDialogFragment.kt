package com.example.weatherapp.ui.dialog

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentChooseDialogBinding
import com.example.weatherapp.ui.splash.SplashViewModel
import com.example.weatherapp.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChooseDialogFragment : DialogFragment()  {

    private var _binding: FragmentChooseDialogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DialogViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.round_corner);
        _binding = FragmentChooseDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBackButton()

        binding.btnOk.setOnClickListener {
            if (binding.radioGroup.checkedRadioButtonId == R.id.radio_gps) {
                viewModel.putIsMapBoolean(Constants.IS_MAP, false)
            } else if (binding.radioGroup.checkedRadioButtonId == R.id.radio_maps) {
                viewModel.putIsMapBoolean(Constants.IS_MAP, true)
            }
            startMainActivity()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog!!.setCanceledOnTouchOutside(false)
        dialog!!.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun startMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun handleBackButton() {
        binding.root.isFocusableInTouchMode = true
        binding.root.requestFocus()
        binding.root.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                activity?.finish()
                return@OnKeyListener true
            }
            false
        })
    }


}