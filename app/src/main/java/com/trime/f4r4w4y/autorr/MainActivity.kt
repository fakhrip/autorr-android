package com.trime.f4r4w4y.autorr

import android.annotation.SuppressLint
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
    private var loadingText: TextView? = null
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
        loadingText = findViewById(R.id.loading_text)

        resetUI()
    }

    private fun resetUI(isCancelling: Boolean = false) {
        isRunning = false
        isFinished = false
        progressText?.setText(R.string.placeholder_text)
        controllerButton?.setText(R.string.start)
        loadingBar?.progress = 0
        loadingText?.setText(R.string._0_100)

        if (isCancelling) sViewModel.cancelJob()
    }

    // Sorry this function name is
    // really misleading XD
    @SuppressLint("SetTextI18n")
    private fun finishUI(result: String) {
        isRunning = true
        isFinished = true
        loadingBar?.progress = 100
        loadingText?.setText(R.string._100_100)
        controllerButton?.setText(R.string.finish)
        progressText?.text = "${getString(R.string.result_text)}\n$result"
    }

    private fun runAcquisitionProcess() {
        isRunning = true
        isFinished = false
        progressText?.setText(R.string.wait_text)
        controllerButton?.setText(R.string.stop)
        loadingBar?.progress = 0
        loadingText?.setText(R.string._0_100)

        val libs: Array<String> =
            arrayOf(fUtil.loadAssetFile("math.js"), fUtil.loadAssetFile("numeric.js"))
        sViewModel.getAndEvaluateData(
            rInterpreter,
            fUtil.loadAssetFile("calculation.js"),
            "startCalculation",
            libs,
            loadingBar,
            progressText,
            loadingText,
            this::finishUI
        )
    }

    override fun onPause() {
        super.onPause()
        sViewModel.cancelJob()
        sViewModel.unregisterSensors()
    }
}