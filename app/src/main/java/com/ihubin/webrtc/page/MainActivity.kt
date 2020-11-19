package com.ihubin.webrtc.page

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ihubin.webrtc.R
import com.ihubin.webrtc.adapter.OnlineUserAdapter
import com.ihubin.webrtc.databinding.ActivityMainBinding
import com.ihubin.webrtc.socketio.SocketIOHolder
import com.ihubin.webrtc.util.SPUtils
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG: String = MainActivity::class.java.name
    }

    private lateinit var binding: ActivityMainBinding

    private var contactUserName: String? = ""


    var onlineUserList: ArrayList<String>? = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Android WebRTC"
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initSocketIO()

        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ), 0X01
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
        binding.rvOnlineUser.layoutManager = LinearLayoutManager(this)
        binding.rvOnlineUser.adapter = onlineUserList?.let { it ->
            OnlineUserAdapter(it) {
                contactUserName = it.tag as String
                binding.contactUser.text = "当前通信用户：$contactUserName"

                SPUtils.put(this, "contactTo", contactUserName!!)

                binding.startWebrtc.visibility = View.VISIBLE

                val message = JSONObject()
                message.put("type", "userContact")
                message.put("payload", contactUserName)
                SocketIOHolder.emit("command", message)
            }
        }
        binding.startWebrtc.setOnClickListener {
            val message = JSONObject()
            message.put("type", "call")
            message.put("payload", contactUserName)
            SocketIOHolder.emit("command", message)
            startActivity(Intent(this, WebRtcActivity::class.java))
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
            .on("call", onCall)
            .connect(SPUtils.get(this, "login", "") as String)
    }


    private val onConnect = Emitter.Listener { args ->
        Log.d(TAG, "连接建立了")
        val userName = SPUtils.get(this, "login", "")
        runOnUiThread {
            binding.console.text.appendLine("✅与信令服务建立连接了-$userName")
        }
    }

    private val onDisConnect = Emitter.Listener { args ->
        Log.d(TAG, "连接断开了")
        runOnUiThread {
            binding.console.text.appendLine("🚫与信令服务断开连接了")
        }
    }

    private val onConnectError = Emitter.Listener { args ->
        Log.d(TAG, "连接出错：" + args[0])
        runOnUiThread {
            binding.console.text.appendLine("❌与信令服务连接出错了")
        }
    }

    private val onError = Emitter.Listener { args ->
        Log.d(TAG, "出错：" + args[0])
        runOnUiThread {
            binding.console.text.appendLine("❌连接出错了")
        }
    }

    private val onMessage = Emitter.Listener { args ->
        //Log.d(TAG, "收到消息：" + args[0])
        runOnUiThread {
            binding.console.text.appendLine("❤️收到消息: " + args[0])
        }
    }

    private val onUserList = Emitter.Listener { args ->
        runOnUiThread {
            binding.console.text.appendLine("❤️收到用户列表: " + args[0])

            val userListJSONArray: JSONArray = args[0] as JSONArray
            onlineUserList?.clear()
            for (i in 0 until userListJSONArray.length()) {
                onlineUserList?.add(userListJSONArray[i] as String)
            }
            binding.rvOnlineUser.adapter?.notifyDataSetChanged()

        }
    }

    private val onUserContact = Emitter.Listener { args ->
        runOnUiThread {
            contactUserName = args[0] as String
            binding.contactUser.text = "当前通信用户：$contactUserName"

            SPUtils.put(this, "contactTo", contactUserName!!)

            binding.startWebrtc.visibility = View.VISIBLE

            binding.console.text.appendLine("❤️收到通信请求: " + args[0])
        }
    }

    private val onCall = Emitter.Listener { args ->
        runOnUiThread {
            binding.console.text?.appendLine("☎️️收到电话请求: " + args[0])
            startActivity(Intent(this, WebRtcActivity::class.java))
        }
    }

}