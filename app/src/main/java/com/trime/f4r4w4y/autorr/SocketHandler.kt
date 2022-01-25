package com.trime.f4r4w4y.autorr

import android.view.View
import com.google.android.material.snackbar.Snackbar
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketHandler {

    lateinit var mSocket: Socket

    @Synchronized
    fun setSocket(view: View) {
        try {
            mSocket = IO.socket("http://10.0.2.2:3000")
        } catch (e: URISyntaxException) {
            Snackbar.make(
                view,
                "Could not connect to websocket :(",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    @Synchronized
    fun getSocket(): Socket {
        return mSocket
    }

    @Synchronized
    fun establishConnection() {
        mSocket.connect()
    }

    @Synchronized
    fun closeConnection() {
        mSocket.disconnect()
    }
}