package com.ihubin.webrtc.socketio

import com.ihubin.webrtc.Contants
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URISyntaxException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentMap

class SocketIOHolder private constructor() {

    companion object {

        private var mSocket: Socket? = null

        private val callbacks: ConcurrentMap<String, ConcurrentLinkedQueue<Emitter.Listener>> = ConcurrentHashMap()

        fun connect(userName: String) {
            mSocket = try {
                val options: IO.Options = IO.Options()
                options.query = "loginUserName=$userName"
                IO.socket(Contants.SIGNAL_SERVER, options)
            } catch (e: URISyntaxException) {
                null
            }
            for (event in callbacks.keys) {
                mSocket?.on(event) { args ->
                    val cbs: ConcurrentLinkedQueue<Emitter.Listener>? = callbacks[event]
                    if (cbs != null) {
                        for (item in cbs) {
                            item.call(*args)
                        }
                    }
                }
            }
            mSocket?.open()
        }

        fun disconnect() {
            for (event in callbacks.keys) {
                mSocket?.off(event)
            }
            mSocket?.close()
        }

        fun on(event: String?, fn: Emitter.Listener?): Companion {
            var cbs: ConcurrentLinkedQueue<Emitter.Listener>? = callbacks[event]
            if (cbs == null) {
                cbs = ConcurrentLinkedQueue()
                val tempCallbacks: ConcurrentLinkedQueue<Emitter.Listener>? = callbacks.putIfAbsent(event, cbs)
                if (tempCallbacks != null) {
                    cbs = tempCallbacks
                }
            }
            cbs.add(fn)

            if(mSocket != null) {
                mSocket?.on(event) { args ->
                    val cbs2: ConcurrentLinkedQueue<Emitter.Listener>? = callbacks[event]
                    if (cbs2 != null) {
                        for (item in cbs2) {
                            item.call(*args)
                        }
                    }
                }
            }
            return SocketIOHolder
        }

        fun off(event: String?, fn: Emitter.Listener?): Companion {
            val callbacks = callbacks[event]
            if (callbacks != null) {
                val it = callbacks.iterator()
                while (it.hasNext()) {
                    val internal = it.next()
                    if (fn == internal) {
                        it.remove()
                        break
                    }
                }
            }
            return SocketIOHolder
        }

        fun send(message: JSONObject) {
            emit(Socket.EVENT_MESSAGE, message)
        }

        fun emit(event: String, message: JSONObject) {
//            val moshi = Moshi.Builder()
//                .addLast(KotlinJsonAdapterFactory())
//                .build()
//            var content: String = ""
//            if(message is CommandMessage) {
//                val jsonAdapter: JsonAdapter<CommandMessage> = moshi.adapter(CommandMessage::class.java)
//                content = jsonAdapter.toJson(message)
//            } else if(message is SignalMessage) {
//                val jsonAdapter: JsonAdapter<SignalMessage> = moshi.adapter(SignalMessage::class.java)
//                content = jsonAdapter.toJson(message)
//            }
            mSocket?.emit(event, message)
        }

    }


}