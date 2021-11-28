package com.trime.f4r4w4y.autorr

import android.content.Context
import android.util.Log
import java.io.*

class FileUtil(context: Context) {
    private var mContext: Context = context
    fun saveStringToFile(code: String, filename: String) {
        val f = File(mContext.filesDir, filename)
        Log.d("FILE_PATH", f.absolutePath)
        try {
            if (!f.exists()) {
                f.createNewFile()
            }
            val fo = FileOutputStream(f)
            val data = code.toByteArray()
            fo.write(data)
            fo.flush()
            fo.close()
        } catch (ex: FileNotFoundException) {
            Log.e("FILE_ERROR_NOT_FOUND", ex.message.toString())
        } catch (e: IOException) {
            Log.e("FILE_ERROR", e.message.toString())
        }
    }

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