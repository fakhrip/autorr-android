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
import io.socket.client.Socket
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var sViewModel: SensorViewModel
    private lateinit var fUtil: FileUtil
    private var progressText: TextView? = null
    private var loadingText: TextView? = null
    private var uidText: TextView? = null
    private var controllerButton: Button? = null
    private var generateButton: Button? = null
    private var loadingBar: LinearProgressIndicator? = null
    private var isRunning: Boolean = false
    private var isFinished: Boolean = false
    private var mSocket: Socket? = null
    private var socketUID: String = ""

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
        fUtil = FileUtil(applicationContext)

        if (!isOnline()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "No internet connections available :(",
                Snackbar.LENGTH_LONG
            ).show()
        }

        SocketHandler.setSocket(findViewById(android.R.id.content))
        SocketHandler.establishConnection()
        mSocket = SocketHandler.getSocket()

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

        generateButton?.setOnClickListener {
            socketUID = getRandomString()
            uidText?.text = socketUID
        }
    }

    private fun getRandomString(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..6)
            .map { allowedChars.random() }
            .joinToString("")
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
        generateButton = findViewById(R.id.generate_button)
        progressText = findViewById(R.id.progress_text)
        loadingBar = findViewById(R.id.loading_bar)
        loadingText = findViewById(R.id.loading_text)
        uidText = findViewById(R.id.uid_text)

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
        if (socketUID == "") {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Generate websocket uid first...",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

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
            mSocket,
            socketUID,
            this::finishUI
        )
    }

    override fun onStop() {
        super.onStop()

        SocketHandler.closeConnection()
    }

    override fun onPause() {
        super.onPause()

        if (this::sViewModel.isInitialized) {
            sViewModel.cancelJob()
            sViewModel.unregisterSensors()
        }
    }
}