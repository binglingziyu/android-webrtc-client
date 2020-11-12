package com.ihubin.webrtc.page

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ihubin.webrtc.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "登录"
        setContentView(R.layout.activity_login)

    }

    fun login(view: View) {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }


}