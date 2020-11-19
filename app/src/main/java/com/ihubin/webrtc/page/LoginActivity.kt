package com.ihubin.webrtc.page

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ihubin.webrtc.databinding.ActivityLoginBinding
import com.ihubin.webrtc.util.SPUtils

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "登录"
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.loginBtn.setOnClickListener {
            if(binding.nameInput.text == null || binding.nameInput.text.isEmpty()) {
                Toast.makeText(this, "填写用户名", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            SPUtils.put(this, "login", binding.nameInput.text)

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }


}