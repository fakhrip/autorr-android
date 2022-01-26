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
            mSocket = IO.socket("http://192.168.0.9:7777")
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