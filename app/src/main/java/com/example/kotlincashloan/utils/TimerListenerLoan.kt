package com.example.kotlincashloan.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import com.example.kotlincashloan.ui.registration.login.HomeActivity
import com.example.kotlinscreenscanner.ui.MainActivity
import com.regula.documentreader.api.DocumentReader
import com.timelysoft.tsjdomcom.service.AppPreferences


class TimerListenerLoan(var activity: Activity){
    private lateinit var timer: CountDownTimer
    private val handler = Handler()
    var numberContractions = 0

    init {
        try {
            timer = object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
//                    Toast.makeText(activity.applicationContext, millisUntilFinished.toString() , Toast.LENGTH_LONG).show()
//                    Toast.makeText(activity.applicationContext, "seconds remaining: " + millisUntilFinished / 1000, Toast.LENGTH_LONG).show()
                }

                override fun onFinish() {
                    handler.postDelayed(Runnable { // Do something after 5s = 500ms
                        MainActivity.timer.timeStart()
                    }, 2200)
                    AppPreferences.token = ""
                    HomeActivity.repeatedClick = 1
                    AppPreferences.isNumber = true
                    AppPreferences.isPinCode = true
                    DocumentReader.Instance().stopScanner(activity.applicationContext);
                    activity.finishAffinity();
                    System.exit(0);
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun timeStart() {
        try {
            timer.start()
        } catch (e: Exception) {

        }
    }

    fun timeStop() {
        try {
            timer.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}