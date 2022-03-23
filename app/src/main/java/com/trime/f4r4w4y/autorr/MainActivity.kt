package com.trime.f4r4w4y.autorr

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.progressindicator.LinearProgressIndicator

class MainActivity : AppCompatActivity() {
    private lateinit var sViewModel: SensorViewModel
    private lateinit var fUtil: FileUtil
    private var progressText: TextView? = null
    private var loadingText: TextView? = null
    private var controllerButton: Button? = null
    private var changeButton: Button? = null
    private var loadingBar: LinearProgressIndicator? = null

    private var isRunning: Boolean = false
    private var isFinished: Boolean = false

    private var calculationType: String = "respiration_rate"

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

        controllerButton?.setOnClickListener {
            if (!isRunning) runAcquisitionProcess()
            else if (isFinished) resetUI()
            else resetUI(true)
        }

        changeButton?.setOnClickListener {
            calculationType =
                if (calculationType == "respiration_rate") "heart_rate" else "respiration_rate"

            resetUI()
        }
    }

    private fun setupUI() {
        controllerButton = findViewById(R.id.controller_button)
        progressText = findViewById(R.id.progress_text)
        loadingBar = findViewById(R.id.loading_bar)
        loadingText = findViewById(R.id.loading_text)
        changeButton = findViewById(R.id.change_button)

        resetUI()
    }

    private fun resetUI(isCancelling: Boolean = false) {
        isRunning = false
        isFinished = false
        val text = makeSectionOfTextBold(
            (if (calculationType == "respiration_rate") getString(R.string.placeholderRR_text) else getString(
                R.string.placeholderHR_text
            )).toString(),
            if (calculationType == "respiration_rate") "respiration rate" else "heart rate"
        )
        progressText?.text = text
        controllerButton?.setText(R.string.start)
        loadingBar?.progress = 0
        loadingText?.setText(R.string._0_100)
        changeButton?.isEnabled = true

        if (isCancelling) sViewModel.cancelJob()
    }

    private fun makeSectionOfTextBold(text: String, textToBold: String): SpannableStringBuilder? {
        val builder = SpannableStringBuilder()
        if (textToBold.isNotEmpty() && textToBold.trim { it <= ' ' } != "") {

            //for counting start/end indexes
            val testText = text.lowercase()
            val testTextToBold = textToBold.lowercase()
            val startingIndex = testText.indexOf(testTextToBold)
            val endingIndex = startingIndex + testTextToBold.length
            //for counting start/end indexes
            if (startingIndex < 0 || endingIndex < 0) {
                return builder.append(text)
            } else if (startingIndex >= 0 && endingIndex >= 0) {
                builder.append(text)
                builder.setSpan(StyleSpan(Typeface.BOLD), startingIndex, endingIndex, 0)
            }
        } else {
            return builder.append(text)
        }
        return builder
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
        changeButton?.isEnabled = true

        val text = makeSectionOfTextBold(
            (if (calculationType == "respiration_rate") getString(R.string.resultRR_text) else getString(
                R.string.resultHR_text
            )).toString() + "\n\n$result",
            if (calculationType == "respiration_rate") "respiration rate" else "heart rate"
        )
        progressText?.text = text
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
        changeButton?.isEnabled = false

        val libs: Array<String> =
            arrayOf(
                fUtil.loadAssetFile("math.js"),
                fUtil.loadAssetFile("bessel.js"),
                fUtil.loadAssetFile("numeric.js"),
                fUtil.loadAssetFile("octave.js")
            )

        sViewModel.getAndEvaluateData(
            if (calculationType == "respiration_rate") fUtil.loadAssetFile("respiration_rate.js") else fUtil.loadAssetFile(
                "heart_rate.js"
            ),
            if (calculationType == "respiration_rate") "startCalculationRR" else "startCalculationHR",
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