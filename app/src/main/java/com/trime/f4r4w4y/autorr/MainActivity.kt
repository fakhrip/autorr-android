package com.trime.f4r4w4y.autorr

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.trime.f4r4w4y.autorr.gql.QueryViewModel
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var sViewModel: SensorViewModel
    private lateinit var qViewModel: QueryViewModel
    private lateinit var fUtil: FileUtil
    private var progressText: TextView? = null
    private var loadingText: TextView? = null
    private var controllerButton: Button? = null
    private var loadingBar: LinearProgressIndicator? = null
    private var isRunning: Boolean = false
    private var isFinished: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle the splash screen transition.
        val splashScreen = installSplashScreen()
        splashScreen.setOnExitAnimationListener {
            val pref = getSharedPreferences("autorr_pref", MODE_PRIVATE)

            // Check for first timer, open AppIntro if so
            val isFirstStart = pref.getBoolean("first_start", true)
            if (isFirstStart) {
                startActivity(Intent(applicationContext, AutorrAppIntro::class.java))
                finish()
                return@setOnExitAnimationListener
            }

            it.remove()
        }

        setContentView(R.layout.activity_main)

        setupUI()
        sViewModel = ViewModelProvider(this)[SensorViewModel::class.java]
        qViewModel = ViewModelProvider(this)[QueryViewModel::class.java]
        fUtil = FileUtil(applicationContext)

        if (!isOnline()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "No internet connections available :(",
                Snackbar.LENGTH_LONG
            ).show()
        }

        controllerButton?.setOnClickListener {
            if (!isOnline()) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "No internet connections available :(",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (!isRunning) runAcquisitionProcess()
            else if (isFinished) resetUI()
            else resetUI(true)
        }
    }

    fun isOnline(): Boolean {
        val runtime = Runtime.getRuntime()
        try {
            val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
            val exitValue = ipProcess.waitFor()
            return exitValue == 0
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return false
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
        // Its okay to sleep after whole process finished
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        isRunning = true
        isFinished = true
        loadingBar?.isIndeterminate = false
        loadingBar?.progress = 100
        loadingText?.setText(R.string._100_100)
        controllerButton?.isEnabled = true
        controllerButton?.setText(R.string.finish)
        progressText?.text = "${getString(R.string.result_text)}\n\n$result"
    }

    private fun runAcquisitionProcess() {
        // Set screen to always on during acquisition process
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        isRunning = true
        isFinished = false
        progressText?.setText(R.string.wait_text)
        controllerButton?.setText(R.string.stop)
        loadingBar?.progress = 0
        loadingText?.setText(R.string._0_100)

        val libs: Array<String> =
            arrayOf(
                fUtil.loadAssetFile("math.js"),
                fUtil.loadAssetFile("bessel.js"),
                fUtil.loadAssetFile("numeric.js")
            )
        sViewModel.getAndEvaluateData(
            fUtil.loadAssetFile("calculation.js"),
            "startCalculation",
            libs,
            loadingBar,
            progressText,
            loadingText,
            controllerButton,
            this::finishUI
        )
    }

    override fun onPause() {
        super.onPause()

        if (this::sViewModel.isInitialized) {
            sViewModel.cancelJob()
            sViewModel.unregisterSensors()
        }
    }
}