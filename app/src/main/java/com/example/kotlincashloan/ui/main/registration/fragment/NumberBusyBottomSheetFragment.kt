package com.example.kotlinscreenscanner.ui.login.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlincashloan.R
import com.example.kotlincashloan.ui.main.registration.login.MainActivity
import com.example.kotlincashloan.ui.main.registration.recovery.ContactingServiceActivity
import com.example.kotlincashloan.ui.main.registration.recovery.PasswordRecoveryActivity
import com.example.myapplication.LoginViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.timelysoft.tsjdomcom.utils.LoadingAlert
import kotlinx.android.synthetic.main.fragment_number_busy_bottom_sheet.*

class NumberBusyBottomSheetFragment() : BottomSheetDialogFragment() {
    private var viewModel = LoginViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_number_busy_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MainActivity.alert = LoadingAlert(activity as AppCompatActivity)
        initClick()
    }

    private fun initClick() {
        number_busy_esc.setOnClickListener {
            MainActivity.alert.show()
            this.dismiss()
            MainActivity.alert.hide()
        }

        number_busy_password.setOnClickListener {
            MainActivity.alert.show()
            val intent = Intent(context, PasswordRecoveryActivity::class.java)
            startActivity(intent)
            MainActivity.alert.hide()
        }

        number_busy_support.setOnClickListener {
            MainActivity.alert.show()
            val intent = Intent(context, ContactingServiceActivity::class.java)
            startActivity(intent)
            MainActivity.alert.hide()
        }
    }
}