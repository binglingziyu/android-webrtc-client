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
import com.ihubin.webrtc.socketio.SocketIOHolder
import com.ihubin.webrtc.util.SPUtils
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.webrtc.PeerConnectionFactory
import java.net.URISyntaxException
import java.util.concurrent.ConcurrentLinkedQueue


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

        // 连接信令服务器
        SocketIOHolder
                .on(Socket.EVENT_CONNECT, onConnect)
                .on(Socket.EVENT_DISCONNECT, onDisConnect)
                .on(Socket.EVENT_CONNECT_ERROR, onConnectError)
                .on(Socket.EVENT_ERROR, onError)
                .connect(SPUtils.get(this, "login", "") as String)



        PeerConnectionFactory.initialize(
            PeerConnectionFactory
                .InitializationOptions
                .builder(this)
                .createInitializationOptions()
        )

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


}