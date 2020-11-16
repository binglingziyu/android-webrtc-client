package com.ihubin.webrtc.model

data class SignalMessage(
        val type:String,
        val content: String,
) : BaseMessage()