package com.ihubin.webrtc.socketio

import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.thread.EventThread
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
                IO.socket("http://192.168.3.76:19090/", options)
            } catch (e: URISyntaxException) {
                null
            }
            mSocket?.on(Socket.EVENT_CONNECT, onConnect)
            mSocket?.on(Socket.EVENT_DISCONNECT, onDisConnect)
            mSocket?.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
            mSocket?.on(Socket.EVENT_ERROR, onError)
            mSocket?.open()
        }

        fun disconnect() {
            mSocket?.off(Socket.EVENT_CONNECT, onConnect)
            mSocket?.off(Socket.EVENT_DISCONNECT, onDisConnect)
            mSocket?.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
            mSocket?.off(Socket.EVENT_ERROR, onError)
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

        fun send(content: String) {
            mSocket?.send(content)
        }

        fun emit(event: String, content: String) {
            mSocket?.emit(event, content)
        }

        private val onConnect = Emitter.Listener { args ->
            val cbs: ConcurrentLinkedQueue<Emitter.Listener>? = callbacks[Socket.EVENT_CONNECT]
            if (cbs != null) {
                for (item in cbs) {
                    item.call(args)
                }
            }
        }

        private val onDisConnect = Emitter.Listener { args ->
            val cbs: ConcurrentLinkedQueue<Emitter.Listener>? = callbacks[Socket.EVENT_DISCONNECT]
            if (cbs != null) {
                for (item in cbs) {
                    item.call(args)
                }
            }
        }

        private val onConnectError = Emitter.Listener { args ->
            val cbs: ConcurrentLinkedQueue<Emitter.Listener>? = callbacks[Socket.EVENT_CONNECT_ERROR]
            if (cbs != null) {
                for (item in cbs) {
                    item.call(args)
                }
            }
        }

        private val onError = Emitter.Listener { args ->
            val cbs: ConcurrentLinkedQueue<Emitter.Listener>? = callbacks[Socket.EVENT_ERROR]
            if (cbs != null) {
                for (item in cbs) {
                    item.call(args)
                }
            }
        }

    }


}