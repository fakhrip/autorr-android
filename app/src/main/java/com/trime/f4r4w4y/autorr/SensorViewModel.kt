package com.trime.f4r4w4y.autorr

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SensorViewModel(application: Application) : AndroidViewModel(application),
    SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var gyroscope: Sensor
    private lateinit var accelerometer: Sensor

    private var gravity = FloatArray(3) { 0F }
    private var accValue = FloatArray(3) { 0F }
    private var gyrValue = FloatArray(3) { 0F }

    fun registerSensors() {
        sensorManager =
            getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.let {
            this.gyroscope = it
        } ?: run {
            Snackbar.make(
                View(getApplication<Application>().applicationContext),
                "Your device have no gyroscope sensors !!!",
                Snackbar.LENGTH_LONG
            ).show()
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            this.accelerometer = it
        } ?: run {
            Snackbar.make(
                View(getApplication<Application>().applicationContext),
                "Your device have no accelerometer sensors !!!",
                Snackbar.LENGTH_LONG
            ).show()
        }

        sensorManager.registerListener(
            this,
            this.gyroscope,
            SensorManager.SENSOR_DELAY_FASTEST
        )

        sensorManager.registerListener(
            this,
            this.accelerometer,
            SensorManager.SENSOR_DELAY_FASTEST
        )
    }

    fun unregisterSensors() {
        sensorManager.unregisterListener(this)
    }

    fun getAndEvaluateData(
        rhinoInterpreter: RhinoInterpreter,
        jsCode: String,
        funcName: String,
        libs: Array<String>
    ) {
        viewModelScope.launch {
            registerSensors()
            Log.d("SENSOR_R", "counting ...")
            val sensorData = getSensorData(6000) // Get and evaluate 1 minute data
            val (timeArr, accX, accY, accZ, gyrX, gyrY, gyrZ) = sensorData
            Log.d("SENSOR_R", "sensor_data: ${sensorData.contentDeepToString()}")
            unregisterSensors()

            // Load all the libraries first
            libs.forEach {
                rhinoInterpreter.loadLib(it)
            }

            // Load the main script
            rhinoInterpreter.evalScript(jsCode, "javascriptEvaluation")

            // Run the corresponding function
            val result: Double = rhinoInterpreter.callJsFunction(
                funcName,
                timeArr,
                accX,
                accY,
                accZ,
                gyrX,
                gyrY,
                gyrZ
            )
            Log.d("SENSOR_R", "result: $result")
        }
    }

    private suspend fun getSensorData(dataSize: Int): Array<FloatArray> {
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

        while (accX.size != dataSize) {
            delay(1) // Weirdly needed, to let the sensor update the value first
            if (time == 0L) time = System.currentTimeMillis()

            val seconds =
                String.format("%.2f", (System.currentTimeMillis() - time) / 1000F).toFloat()
            if (!"%.2f".format(lastSeconds).contentEquals("%.2f".format(seconds))) {
                lastSeconds = seconds

                timeArr = timeArr.plus(seconds)
                accX = accX.plus(accValue[0])
                accY = accY.plus(accValue[1])
                accZ = accZ.plus(accValue[2])
                gyrX = gyrX.plus(gyrValue[0])
                gyrY = gyrY.plus(gyrValue[0])
                gyrZ = gyrZ.plus(gyrValue[0])
            }
        }

        return arrayOf(timeArr, accX, accY, accZ, gyrX, gyrY, gyrZ)
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
