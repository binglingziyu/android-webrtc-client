package com.ihubin.webrtc

import android.app.Application
import android.util.Log
import org.webrtc.PeerConnectionFactory

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("MainApplication", "onCreate")

        PeerConnectionFactory.initialize(
            PeerConnectionFactory
            .InitializationOptions
            .builder(this)
            .setEnableInternalTracer(true)
            .createInitializationOptions());
    }

}