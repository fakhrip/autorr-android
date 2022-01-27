package com.trime.f4r4w4y.autorr

import android.view.View
import com.google.android.material.snackbar.Snackbar
import io.socket.client.IO
import io.socket.client.Socket

object SocketHandler {

    lateinit var mSocket: Socket
    var isConnected: Boolean = false

    @Synchronized
    fun setSocket(view: View, url: String) {
        try {
            mSocket = IO.socket(url)
            isConnected = false
        } catch (e: Exception) {
            Snackbar.make(
                view,
                "Could not connect to websocket :(",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    @Synchronized
    fun getSocket(): Socket? {
        if (this::mSocket.isInitialized) {
            return mSocket
        }

        return null
    }

    @Synchronized
    fun establishConnection() {
        if (this::mSocket.isInitialized) {
            mSocket.connect()
            isConnected = true
        }
    }

    @Synchronized
    fun closeConnection() {
        if (this::mSocket.isInitialized) {
            mSocket.disconnect()
            isConnected = false
        }
    }

    @Synchronized
    fun enterRoom(room: String) {
        mSocket.emit("listen_data", room)
    }

    @Synchronized
    fun leaveRoom(room: String) {
        mSocket.emit("unlisten_data", room)
    }

    @Synchronized
    fun changeRoom(prev_room: String, next_room: String) {
        this.leaveRoom(prev_room)
        this.enterRoom(next_room)
    }
}