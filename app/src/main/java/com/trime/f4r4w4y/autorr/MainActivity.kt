package com.trime.f4r4w4y.autorr

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var sViewModel: SensorViewModel
    private lateinit var rInterpreter: RhinoInterpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sViewModel = ViewModelProvider(this)[SensorViewModel::class.java]
        rInterpreter = RhinoInterpreter()

        val libs: Array<String> = arrayOf(loadAssetFile("math.js"), loadAssetFile("numeric.js"))
        sViewModel.getAndEvaluateData(
            rInterpreter,
            loadAssetFile("calculation.js"),
            "startCalculation",
            libs
        )
    }

    override fun onPause() {
        super.onPause()
        sViewModel.unregisterSensors()
    }

    fun loadAssetFile(inFile: String): String {
        var tContents = ""

        try {
            val stream: InputStream = assets.open(inFile)
            val size: Int = stream.available()
            val buffer = ByteArray(size)

            stream.read(buffer)
            stream.close()

            tContents = String(buffer)
        } catch (e: IOException) {
            Log.e("LOAD_FILE_ERROR", e.message!!)
        }

        return tContents
    }
}