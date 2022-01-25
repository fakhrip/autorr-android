package com.trime.f4r4w4y.autorr

import android.content.Context
import android.util.Log
import java.io.IOException
import java.io.InputStream

class FileUtil(context: Context) {
    private var mContext: Context = context

    fun loadAssetFile(inFile: String): String {
        var tContents = ""

        try {
            val stream: InputStream = mContext.assets.open(inFile)
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