package com.ihubin.webrtc.page

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ihubin.webrtc.R
import com.ihubin.webrtc.util.SPUtils

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "设置"
        setContentView(R.layout.activity_setting)
    }

    fun logout(view: View) {
        SPUtils.remove(this, "login")

        val loginIntent = Intent(this, LoginActivity::class.java)
        loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(loginIntent)
    }


}