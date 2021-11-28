package com.trime.f4r4w4y.autorr

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.progressindicator.LinearProgressIndicator

class MainActivity : AppCompatActivity() {
    private lateinit var sViewModel: SensorViewModel
    private lateinit var rInterpreter: RhinoInterpreter
    private lateinit var fUtil: FileUtil
    private var progressText: TextView? = null
    private var controllerButton: Button? = null
    private var loadingBar: LinearProgressIndicator? = null
    private var isRunning: Boolean = false
    private var isFinished: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupUI()
        sViewModel = ViewModelProvider(this)[SensorViewModel::class.java]
        fUtil = FileUtil(applicationContext)
        rInterpreter = RhinoInterpreter()

        controllerButton?.setOnClickListener {
            if (!isRunning) runAcquisitionProcess()
            else if (isFinished) resetUI()
            else resetUI(true)
        }
    }

    private fun setupUI() {
        controllerButton = findViewById(R.id.controller_button)
        progressText = findViewById(R.id.progress_text)
        loadingBar = findViewById(R.id.loading_bar)

        resetUI()
    }

    private fun resetUI(isCancelling: Boolean = false) {
        isRunning = false
        isFinished = false
        progressText?.setText(R.string.placeholder_text)
        controllerButton?.setText(R.string.start)
        loadingBar?.progress = 0

        if (isCancelling) sViewModel.cancelJob()
    }

    // Sorry this function name is
    // really misleading XD
    private fun finishUI() {
        isRunning = true
        isFinished = true
        loadingBar?.progress = 100
        controllerButton?.setText(R.string.finish)
    }

    private fun runAcquisitionProcess() {
        isRunning = true
        isFinished = false
        progressText?.setText(R.string.wait_text)
        controllerButton?.setText(R.string.stop)
        loadingBar?.progress = 0

        val libs: Array<String> =
            arrayOf(fUtil.loadAssetFile("math.js"), fUtil.loadAssetFile("numeric.js"))
        sViewModel.getAndEvaluateData(
            rInterpreter,
            fUtil.loadAssetFile("calculation.js"),
            "startCalculation",
            libs,
            loadingBar,
            progressText,
            this::finishUI
        )
    }

    override fun onPause() {
        super.onPause()
        sViewModel.cancelJob()
        sViewModel.unregisterSensors()
    }
}