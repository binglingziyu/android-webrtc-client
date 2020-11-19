package com.ihubin.webrtc.page

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ihubin.webrtc.R
import com.ihubin.webrtc.databinding.ActivitySettingBinding
import com.ihubin.webrtc.util.SPUtils
import com.sloydev.preferator.Preferator

class SettingActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "设置"
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.sharedPreferences.setOnClickListener {
            Preferator.launch(this)
        }
        binding.logout.setOnClickListener {
            SPUtils.remove(this, "login")

            val loginIntent = Intent(this, LoginActivity::class.java)
            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(loginIntent)
        }
    }

}