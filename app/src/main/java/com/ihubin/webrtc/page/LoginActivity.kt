package com.ihubin.webrtc.page

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ihubin.webrtc.R

class LoginActivity : AppCompatActivity() {

    private var nameInput: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "登录"
        setContentView(R.layout.activity_login)

        nameInput = findViewById(R.id.name_input)

    }

    fun login(view: View) {
        if(nameInput?.text == null || nameInput?.text?.length == 0) {
            Toast.makeText(this, "填写用户名", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }


}