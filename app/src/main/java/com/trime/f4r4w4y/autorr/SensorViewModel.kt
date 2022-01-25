package com.trime.f4r4w4y.autorr

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import io.socket.client.Socket
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

    companion object {
        private const val dataSize: Int = 6000 // takes 1 minute to acquired
    }

    private fun registerSensors() {
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
        socket: Socket?,
        socketUID: String,
        finishCallback: (result: String) -> Unit
    ) {
        fUtil = FileUtil(getApplication<Application>().applicationContext)
        job = viewModelScope.launch {
            registerSensors()

            // Get sensor data for 1 minute
            val sensorData = getSensorData(
                dataSize,
                loadingBar,
                progressText,
                loadingText,
                controllerButton,
                socket,
                socketUID
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
        val result: Any? = rhinoInterpreter.callJsFunction(
            funcName,
            timeArr,
            accX,
            accY,
            accZ,
            gyrX,
            gyrY,
            gyrZ
        )

        return@withContext result as T
    }

    @SuppressLint("SetTextI18n")
    private suspend fun getSensorData(
        dataSize: Int,
        loadingBar: LinearProgressIndicator?,
        progressText: TextView?,
        loadingText: TextView?,
        controllerButton: Button?,
        socket: Socket?,
        socketUID: String
    ): Array<FloatArray> {
        delay(1000) // Weirdly needed, to let the sensor register itself first
        var time = 0L
        var lastSeconds = String.format("%.2f", -1F).toFloat()

        var timeArr: FloatArray = floatArrayOf()
        var accX: FloatArray = floatArrayOf()
        var accY: FloatArray = floatArrayOf()
        var accZ: FloatArray = floatArrayOf()
        var gyrX: FloatArray = floatArrayOf()
        var gyrY: FloatArray = floatArrayOf()
        var gyrZ: FloatArray = floatArrayOf()

        while (timeArr.size != dataSize) {
            delay(1) // Weirdly needed, to let the sensor update the value first
            if (time == 0L) time = System.currentTimeMillis()

            val seconds =
                String.format("%.2f", (System.currentTimeMillis() - time) / 1000F).toFloat()

            val progress = ((timeArr.size.toDouble() / dataSize.toDouble()) * 100).roundToInt()
            loadingText?.text = "${progress}/100"
            loadingBar?.progress = progress

            // Dirty trick because apparently it didn't work if you put it
            // in other place without rendering it multiple times ¯\_(ツ)_/¯
            if (progress > 98)
                progressText?.text =
                    getApplication<Application>().applicationContext.getString(R.string.wait2_text)

            // Reset calculation if first two frequencies is not aligned correctly
            if (timeArr.size > 1 && !"%.2f".format(timeArr[1] - timeArr[0]).contentEquals("0.01")) {
                time = 0L
                lastSeconds = String.format("%.2f", -1F).toFloat()
                timeArr = floatArrayOf()
                accX = floatArrayOf()
                accY = floatArrayOf()
                accZ = floatArrayOf()
                gyrX = floatArrayOf()
                gyrY = floatArrayOf()
                gyrZ = floatArrayOf()
            }

            if (!"%.2f".format(lastSeconds).contentEquals("%.2f".format(seconds))) {
                lastSeconds = seconds

                timeArr = timeArr.plus(seconds)
                accX = accX.plus(accValue[0])
                accY = accY.plus(accValue[1])
                accZ = accZ.plus(accValue[2])
                gyrX = gyrX.plus(gyrValue[0])
                gyrY = gyrY.plus(gyrValue[1])
                gyrZ = gyrZ.plus(gyrValue[2])

                socket?.emit(socketUID, "$timeArr,$accX,$accY,$accZ,$gyrX,$gyrY,$gyrZ")
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

            // Remove the gravity contribution with the high-pass filter.
            accValue[0] = event.values[0] - gravity[0]
            accValue[1] = event.values[1] - gravity[1]
            accValue[2] = event.values[2] - gravity[2] - gravity[2]
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
