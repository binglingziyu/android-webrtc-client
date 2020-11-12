package com.ihubin.webrtc

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.PeerConnectionFactory
import java.net.URISyntaxException


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ), 0X01
        )

        PeerConnectionFactory.initialize(
            PeerConnectionFactory
                .InitializationOptions
                .builder(this)
                .createInitializationOptions()
        )

        val mSocket: Socket = try {
            val options: IO.Options = IO.Options()
            options.query = "loginUserNum=88"
            IO.socket("http://192.168.3.76:19090/", options)
//            IO.socket("https://socketio-chat-h9jt.herokuapp.com/", options)
        } catch (e: URISyntaxException) {
            //throw RuntimeException(e)
            return
        }
        mSocket.on(Manager.EVENT_OPEN, onOpen)
        mSocket.on(Manager.EVENT_CLOSE, onClose)
        mSocket.on(Manager.EVENT_CONNECT_ERROR, onConnectError)
        mSocket.on(Manager.EVENT_ERROR, onError)
        mSocket.open()
    }

    private val onOpen = Emitter.Listener { args ->
        Log.d("MainActivity", "连接打开了")
    }

    private val onClose = Emitter.Listener { args ->
        Log.d("MainActivity", "连接关闭了")
    }

    private val onConnectError = Emitter.Listener { args ->
        Log.d("MainActivity", "连接出错1" + args[0])
    }

    private val onError = Emitter.Listener { args ->
        Log.d("MainActivity", "出错" + args[0])
    }

    fun createPeerConnectionFactory(): PeerConnectionFactory? {
        val builder = PeerConnectionFactory.builder()
//            .setVideoEncoderFactory(encoderFactory)
//            .setVideoDecoderFactory(decoderFactory)

        return builder.createPeerConnectionFactory()
    }

}