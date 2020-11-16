package com.ihubin.webrtc.page

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ihubin.webrtc.R
import com.ihubin.webrtc.adapter.OnlineUserAdapter
import com.ihubin.webrtc.model.CommandMessage
import com.ihubin.webrtc.socketio.SocketIOHolder
import com.ihubin.webrtc.util.SPUtils
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.webrtc.PeerConnectionFactory
import java.lang.reflect.Type

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = MainActivity::class.java.name
    }

    var contactUserName: String? = ""
    var contactUser: TextView? = null
    var console: EditText? = null
    var rvOnlineUser: RecyclerView? = null
    var onlineUserList: ArrayList<String>? = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Android WebRTC"
        setContentView(R.layout.activity_main)

        initView()
        initSocketIO()

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

    private fun initView() {
        contactUser = findViewById(R.id.contact_user)
        rvOnlineUser = findViewById(R.id.rv_online_user)
        console = findViewById(R.id.console)
        rvOnlineUser?.layoutManager = LinearLayoutManager(this)
        rvOnlineUser?.adapter = onlineUserList?.let { it ->
            OnlineUserAdapter(it) {
                contactUserName = it.tag as String
                contactUser?.text = "当前通信用户：$contactUserName"

                val commandMessage = CommandMessage("userContact", payload = contactUserName!!)

                SocketIOHolder.emit("command", commandMessage)
            }
        }
    }

    private fun initSocketIO() {
        // 连接信令服务器
        SocketIOHolder
            .on(Socket.EVENT_CONNECT, onConnect)
            .on(Socket.EVENT_DISCONNECT, onDisConnect)
            .on(Socket.EVENT_CONNECT_ERROR, onConnectError)
            .on(Socket.EVENT_ERROR, onError)
            .on(Socket.EVENT_MESSAGE, onMessage)
            .on("userList", onUserList)
            .on("userContact", onUserContact)
            .connect(SPUtils.get(this, "login", "") as String)
    }


    private val onConnect = Emitter.Listener { args ->
        Log.d(TAG, "连接建立了")
        val userName = SPUtils.get(this, "login", "")
        runOnUiThread {
            console?.text?.appendLine("✅与信令服务建立连接了-$userName")
        }
    }

    private val onDisConnect = Emitter.Listener { args ->
        Log.d(TAG, "连接断开了")
        runOnUiThread {
            console?.text?.appendLine("🚫与信令服务断开连接了")
        }
    }

    private val onConnectError = Emitter.Listener { args ->
        Log.d(TAG, "连接出错：" + args[0])
        runOnUiThread {
            console?.text?.appendLine("❌与信令服务连接出错了")
        }
    }

    private val onError = Emitter.Listener { args ->
        Log.d(TAG, "出错：" + args[0])
        runOnUiThread {
            console?.text?.appendLine("❌连接出错了")
        }
    }

    private val onMessage = Emitter.Listener { args ->
        runOnUiThread {
            console?.text?.appendLine("❤️收到消息: " + args[0])
        }
    }

    private val onUserList = Emitter.Listener { args ->
        runOnUiThread {
            console?.text?.appendLine("❤️收到用户列表: " + args[0])

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val userListJSONArray: JSONArray = args[0] as JSONArray
            val listOfStringType: Type = Types.newParameterizedType(
                List::class.java,
                String::class.java
            )
            val jsonAdapter= moshi.adapter<ArrayList<String>>(listOfStringType)
            val userList: ArrayList<String>? = jsonAdapter.fromJson(userListJSONArray.toString())

            onlineUserList?.clear()
            onlineUserList?.addAll(userList!!)
            rvOnlineUser?.adapter?.notifyDataSetChanged()

        }
    }

    private val onUserContact = Emitter.Listener { args ->
        runOnUiThread {
            contactUserName = args[0] as String
            contactUser?.text = "当前通信用户：$contactUserName"

            console?.text?.appendLine("❤️收到通信请求: " + args[0])
        }
    }

}