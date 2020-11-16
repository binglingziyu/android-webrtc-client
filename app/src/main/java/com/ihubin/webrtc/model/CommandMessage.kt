package com.ihubin.webrtc.model

data class CommandMessage (
        val command: String,
        val payload: String = "",
) : BaseMessage()