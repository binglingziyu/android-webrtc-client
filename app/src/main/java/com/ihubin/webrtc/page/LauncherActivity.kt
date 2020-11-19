package com.ihubin.webrtc.page

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ihubin.webrtc.databinding.ActivityLauncherBinding
import com.ihubin.webrtc.util.SPUtils

class LauncherActivity : AppCompatActivity() {

    lateinit var binding: ActivityLauncherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

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