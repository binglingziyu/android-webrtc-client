package com.ihubin.webrtc.page

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ihubin.webrtc.R
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.webrtc.PeerConnectionFactory
import java.net.URISyntaxException


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Android WebRTC"
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
        mSocket.on(Socket.EVENT_CONNECT, onConnect)
        mSocket.on(Socket.EVENT_DISCONNECT, onDisConnect)
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket.on(Socket.EVENT_ERROR, onError)
        mSocket.open()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.setting -> {            // do something
                startActivity(Intent(this, SettingActivity::class.java))
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private val onConnect = Emitter.Listener { args ->
        Log.d("MainActivity", "连接建立了")
    }

    private val onDisConnect = Emitter.Listener { args ->
        Log.d("MainActivity", "连接断开了")
    }

    private val onConnectError = Emitter.Listener { args ->
        Log.d("MainActivity", "连接出错：" + args[0])
    }

    private val onError = Emitter.Listener { args ->
        Log.d("MainActivity", "出错：" + args[0])
    }

    fun createPeerConnectionFactory(): PeerConnectionFactory? {
        val builder = PeerConnectionFactory.builder()
//            .setVideoEncoderFactory(encoderFactory)
//            .setVideoDecoderFactory(decoderFactory)

        return builder.createPeerConnectionFactory()
    }

}