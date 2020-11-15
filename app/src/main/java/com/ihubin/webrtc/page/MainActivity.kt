package com.ihubin.webrtc.page

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ihubin.webrtc.R
import com.ihubin.webrtc.model.SignalMessage
import com.ihubin.webrtc.socketio.SocketIOHolder
import com.ihubin.webrtc.util.SPUtils
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import org.webrtc.PeerConnectionFactory


class MainActivity : AppCompatActivity() {

    var console: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Android WebRTC"
        setContentView(R.layout.activity_main)

        console = findViewById(R.id.console)

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
                .on(Socket.EVENT_MESSAGE, onMessage)
                .connect(SPUtils.get(this, "login", "") as String)

        PeerConnectionFactory.initialize(
            PeerConnectionFactory
                .InitializationOptions
                .builder(this)
                .createInitializationOptions()
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        SocketIOHolder.disconnect()
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
        val userName = SPUtils.get(this, "login", "")
        runOnUiThread {
            console?.text?.appendLine("✅与信令服务建立连接了-$userName")
        }
    }

    private val onDisConnect = Emitter.Listener { args ->
        Log.d("MainActivity", "连接断开了")
        runOnUiThread {
            console?.text?.appendLine("🚫与信令服务断开连接了")
        }
    }

    private val onConnectError = Emitter.Listener { args ->
        Log.d("MainActivity", "连接出错：" + args[0])
        runOnUiThread {
            console?.text?.appendLine("❌与信令服务连接出错了")
        }
    }

    private val onError = Emitter.Listener { args ->
        Log.d("MainActivity", "出错：" + args[0])
        runOnUiThread {
            console?.text?.appendLine("❌连接出错了")
        }
    }

    private val onMessage = Emitter.Listener { args ->
        runOnUiThread {
            console?.text?.appendLine("❤️收到消息: " + args[0] )
        }
    }

}