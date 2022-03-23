package com.trime.f4r4w4y.autorr

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import org.mozilla.javascript.NativeArray
import kotlin.math.roundToInt

class SensorViewModel(application: Application) : AndroidViewModel(application),
    SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var gyroscope: Sensor
    private lateinit var accelerometer: Sensor
    private lateinit var fUtil: FileUtil
    private lateinit var job: Job

    private var gravity = FloatArray(3) { 0F }
    private var accValue = FloatArray(3) { 0F }
    private var gyrValue = FloatArray(3) { 0F }

    private var calculationType = "respiration_rate"

    companion object {
        private const val dataSizeRR: Int = 6000 // takes 1 minute to acquired
        private const val dataSizeHR: Int = 7500 // takes 1 minute to acquired
    }

    private fun registerSensors(calculationType: String) {
        this.calculationType = calculationType

        sensorManager =
            getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.let {
            this.gyroscope = it

            sensorManager.registerListener(
                this,
                this.gyroscope,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        } ?: run {
            Snackbar.make(
                View(getApplication<Application>().applicationContext),
                "Your device have no gyroscope sensors !!!",
                Snackbar.LENGTH_LONG
            ).show()
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            this.accelerometer = it

            sensorManager.registerListener(
                this,
                this.accelerometer,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        } ?: run {
            Snackbar.make(
                View(getApplication<Application>().applicationContext),
                "Your device have no accelerometer sensors !!!",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    fun unregisterSensors() {
        if (this::sensorManager.isInitialized) sensorManager.unregisterListener(this)
    }

    @SuppressLint("SetTextI18n")
    fun getAndEvaluateData(
        jsCode: String,
        funcName: String,
        libs: Array<String>,
        loadingBar: LinearProgressIndicator?,
        progressText: TextView?,
        loadingText: TextView?,
        controllerButton: Button?,
        finishCallback: (result: String) -> Unit
    ) {
        fUtil = FileUtil(getApplication<Application>().applicationContext)
        job = viewModelScope.launch {
            registerSensors(if (funcName == "startCalculationRR") "respiration_rate" else "heart_rate")

            // Get sensor data for 1 minute
            val sensorData = getSensorData(
                if (funcName == "startCalculationRR") dataSizeRR else dataSizeHR,
                loadingBar,
                progressText,
                loadingText,
                controllerButton
            )

            // Unregister sensor to not waste any batteries
            unregisterSensors()

            // Calculate respiration rate from acquired data and return the result
            val result: NativeArray? = calculateData(jsCode, funcName, libs, sensorData)
            result?.joinToString(",\n")
                ?.let { finishCallback(it) }
        }
    }

    fun cancelJob() {
        if (this::job.isInitialized && job.isActive) {
            job.cancel()
            unregisterSensors()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> calculateData(
        jsCode: String,
        funcName: String,
        libs: Array<String>,
        sensorData: Array<FloatArray>
    ): T? = withContext(Dispatchers.Default) {
        delay(50) // Weirdly needed, to let the suspend works ?
        val (timeArr, accX, accY, accZ, gyrX, gyrY, gyrZ) = sensorData

        val rhinoInterpreter = RhinoInterpreter()

        // Load all the libraries first
        libs.forEach {
            rhinoInterpreter.loadLib(it)
        }

        // Load the main script
        rhinoInterpreter.evalScript(jsCode, "javascriptEvaluation")

        // Run the corresponding function
        val result: Any?
        if (calculationType == "respiration_rate")
            result = rhinoInterpreter.callJsFunction(
                funcName,
                timeArr,
                accX,
                accY,
                accZ,
                gyrX,
                gyrY,
                gyrZ
            )
        else
            result = rhinoInterpreter.callJsFunction(
                funcName,
                timeArr,
                accX,
                accY,
                accZ
            )

        return@withContext result as T
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

    @SuppressLint("SetTextI18n")
    private suspend fun getSensorData(
        dataSize: Int,
        loadingBar: LinearProgressIndicator?,
        progressText: TextView?,
        loadingText: TextView?,
        controllerButton: Button?
    ): Array<FloatArray> {
        delay(1000) // Weirdly needed, to let the sensor register itself first
        val firstVal = if (dataSize == dataSizeRR) "0.01" else "0.008"
        val formatter = if (dataSize == dataSizeRR) "%.2f" else "%.3f"
        val delaySize: Long = if (dataSize == dataSizeRR) 1 else 6

        var time = 0L
        var lastSeconds = String.format(formatter, -1F).toFloat()
        var seconds: Float

        var timeArr: FloatArray = floatArrayOf()
        var accX: FloatArray = floatArrayOf()
        var accY: FloatArray = floatArrayOf()
        var accZ: FloatArray = floatArrayOf()
        var gyrX: FloatArray = floatArrayOf()
        var gyrY: FloatArray = floatArrayOf()
        var gyrZ: FloatArray = floatArrayOf()

        while (timeArr.size != dataSize) {
            delay(delaySize) // Weirdly needed, to let the sensor update the value first
            if (time == 0L) time = System.currentTimeMillis()

            seconds =
                String.format(formatter, (System.currentTimeMillis() - time) / 1000F).toFloat()

            val progress = ((timeArr.size.toDouble() / dataSize.toDouble()) * 100).roundToInt()
            loadingText?.text = "${progress}/100"
            loadingBar?.progress = progress

            // Dirty trick because apparently it didn't work if you put it
            // in other place without rendering it multiple times ¯\_(ツ)_/¯
            if (progress > 98) {
                val text = makeSectionOfTextBold(
                    (if (dataSize == dataSizeRR) getApplication<Application>().applicationContext.getString(
                        R.string.wait2RR_text
                    ) else getApplication<Application>().applicationContext.getString(R.string.wait2HR_text)).toString(),
                    if (dataSize == dataSizeRR) "respiration rate" else "heart rate"
                )

                progressText?.text = text
            }

            if (!formatter.format(lastSeconds).contentEquals(formatter.format(seconds))) {
                lastSeconds = seconds

                timeArr = timeArr.plus(seconds)
                accX = accX.plus(accValue[0])
                accY = accY.plus(accValue[1])
                accZ = accZ.plus(accValue[2])
                gyrX = gyrX.plus(gyrValue[0])
                gyrY = gyrY.plus(gyrValue[1])
                gyrZ = gyrZ.plus(gyrValue[2])

                Log.d(
                    "AAA",
                    "getSensorData: $seconds, ${accValue[0]}, ${accValue[1]}, ${accValue[2]}, ${gyrValue[0]}, ${gyrValue[1]}, ${gyrValue[2]}"
                )
            }

            // Reset calculation if first two frequencies is not aligned correctly
            if (timeArr.size > 1 && !formatter.format(timeArr[1] - timeArr[0])
                    .contentEquals(firstVal)
            ) {
                time = 0L
                lastSeconds = String.format(formatter, -1F).toFloat()
                timeArr = floatArrayOf()
                accX = floatArrayOf()
                accY = floatArrayOf()
                accZ = floatArrayOf()
                gyrX = floatArrayOf()
                gyrY = floatArrayOf()
                gyrZ = floatArrayOf()
            }
        }

        playSound()
        loadingBar?.isIndeterminate = true
        controllerButton?.isEnabled = false

        return arrayOf(timeArr, accX, accY, accZ, gyrX, gyrY, gyrZ)
    }

    private fun playSound(duration: Int = 300) {
        val toneG = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneG.startTone(ToneGenerator.TONE_DTMF_S, duration)
        Handler(Looper.getMainLooper()).postDelayed({
            toneG.release()
        }, (duration + 50).toLong())
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            gyrValue[0] = event.values[0]
            gyrValue[1] = event.values[1]
            gyrValue[2] = event.values[2]
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // In this example, alpha is calculated as t / (t + dT),
            // where t is the low-pass filter's time-constant and
            // dT is the event delivery rate.

            val alpha = 0.8f

            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

            if (calculationType == "respiration_rate") {
                // Remove the gravity contribution with the high-pass filter.
                accValue[0] = event.values[0] - gravity[0]
                accValue[1] = event.values[1] - gravity[1]
                accValue[2] = event.values[2] - gravity[2] - gravity[2]
            } else {
                accValue[0] = event.values[0] * gravity[0]
                accValue[1] = event.values[1] * gravity[1]
                accValue[2] = event.values[2] * gravity[2] - gravity[2]
            }
        }

    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do nothing
    }
}

private operator fun <T> Array<T>.component6(): T {
    return get(5)
}

private operator fun <T> Array<T>.component7(): T {
    return get(6)
}
