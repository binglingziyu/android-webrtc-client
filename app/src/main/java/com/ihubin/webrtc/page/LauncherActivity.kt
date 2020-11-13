package com.ihubin.webrtc.page

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ihubin.webrtc.R
import com.ihubin.webrtc.util.SPUtils

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_launcher)

        val thread = Thread {
            Thread.sleep(1500)
            runOnUiThread {
                if (SPUtils.contains(this, "login")) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                finish()
            }
        }
        thread.start()

    }


}