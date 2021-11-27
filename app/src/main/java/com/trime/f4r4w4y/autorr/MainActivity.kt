package com.trime.f4r4w4y.autorr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {
    private lateinit var sViewModel: SensorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sViewModel: SensorViewModel = ViewModelProvider(this)[SensorViewModel::class.java]
        sViewModel.getAndEvaluateData()
    }

    override fun onPause() {
        super.onPause()
        sViewModel.unregisterSensors()
    }
}