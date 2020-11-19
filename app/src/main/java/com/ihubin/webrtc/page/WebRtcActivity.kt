package com.ihubin.webrtc.page

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ihubin.webrtc.R
import com.ihubin.webrtc.databinding.ActivityStartWebrtcBinding
import com.ihubin.webrtc.socketio.SocketIOHolder
import com.ihubin.webrtc.util.SPUtils
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.*
import org.webrtc.MediaConstraints.KeyValuePair
import org.webrtc.PeerConnection.*
import org.webrtc.PeerConnection.Observer
import java.nio.ByteBuffer
import java.util.*


class WebRtcActivity : AppCompatActivity() {

    companion object {
        const val TAG = "WebRtcActivity"
    }

    lateinit var binding: ActivityStartWebrtcBinding

    var started: Boolean = false

    private var rootEglBase: EglBase? = null
    private var factory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var dataChannel: DataChannel? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var videoCapture: VideoCapturer? = null
    private var videoSource: VideoSource? = null
    private var audioSource: AudioSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartWebrtcBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        start()
    }

    override fun onDestroy() {
        super.onDestroy()

        videoCapture?.stopCapture()
        videoCapture?.dispose()
        videoCapture = null
        videoSource?.dispose()
        videoSource = null
        audioSource?.dispose()
        audioSource = null

        binding.surfaceView.release()
        binding.surfaceView2.release()
        rootEglBase?.releaseSurface()
        rootEglBase?.release()
        rootEglBase = null

        surfaceTextureHelper?.dispose()
        surfaceTextureHelper = null

        dataChannel?.close()
        dataChannel?.dispose()
        dataChannel = null

        peerConnection?.close()
        peerConnection?.dispose()
        peerConnection = null

        factory?.dispose()
        factory = null

    }

    private fun initView() {
        binding.startCall.setOnClickListener {
            doCall()
        }
        binding.startChannel.setOnClickListener {
            if(dataChannel == null) {
                val init = DataChannel.Init();
                init.ordered = true
                init.negotiated = false
                dataChannel = peerConnection?.createDataChannel("dataChannel", init)
                dataChannel?.registerObserver(object : DataChannel.Observer {
                    override fun onBufferedAmountChange(p0: Long) {
                        Log.d(TAG, "########## onBufferedAmountChange: $p0")
                    }

                    override fun onStateChange() {
                        Log.d(TAG, "########## onStateChange" + dataChannel?.state())
                    }

                    override fun onMessage(buffer: DataChannel.Buffer?) {
                        Log.d(TAG, "********** 接收到消息 ***********")
                        val data = buffer!!.data
                        val bytes = ByteArray(data.capacity())
                        data.get(bytes)
                        //data[bytes]
                        val strData = String(bytes)
                        runOnUiThread {
                           Toast.makeText(this@WebRtcActivity, strData, Toast.LENGTH_SHORT).show()
                        }
                    }
                })

            } else {
                sendDataChannelMessage("地瓜地瓜，我是土豆！", dataChannel!!)
            }
        }
    }

    fun sendDataChannelMessage(message: String, dataChannel: DataChannel) {
        Log.d(TAG, "sendDataChannelMessage: ${dataChannel.state()}")
        val msg = message.toByteArray()
        val buffer = DataChannel.Buffer(
                ByteBuffer.wrap(msg), false)
        val sendResult = dataChannel.send(buffer)
        //Toast.makeText(this@WebRtcActivity, "发送结果：$sendResult", Toast.LENGTH_SHORT).show()
    }

    private fun start() {
        initializeSocketIO()
        initializeSurfaceViews()
        initializePeerConnectionFactory()
        createVideoTrackFromCameraAndShowIt()
        initializePeerConnections()
        startStreamingVideo()
    }

    private fun initializeSocketIO() {
        SocketIOHolder.on(Socket.EVENT_MESSAGE) { args ->
            if (args[0] is String) {
                val message = args[0] as String
                if (message == "got user media") {
                    //maybeStart()
                }
            } else {
                val message = args[0] as JSONObject
//                Log.d(TAG, "connectToSignallingServer: got message $message")
                if (message.getString("type") == "offer") {
                    Log.d(TAG, "connectToSignallingServer: received an offer")

                    peerConnection?.setRemoteDescription(
                            object : SdpObserver {
                                override fun onCreateSuccess(p0: SessionDescription?) {}
                                override fun onSetSuccess() {}
                                override fun onCreateFailure(p0: String?) {}
                                override fun onSetFailure(p0: String?) {}
                            },
                            SessionDescription(SessionDescription.Type.OFFER, message.getString("sdp"))
                    )
                    doAnswer()
                } else if (message.getString("type") == "answer") {
                    peerConnection?.setRemoteDescription(
                            object : SdpObserver {
                                override fun onCreateSuccess(p0: SessionDescription?) {}
                                override fun onSetSuccess() {}
                                override fun onCreateFailure(p0: String?) {}
                                override fun onSetFailure(p0: String?) {}
                            },
                            SessionDescription(SessionDescription.Type.ANSWER, message.getString("sdp"))
                    )
                } else if (message.getString("type") == "candidate") {
                    Log.d(TAG, "connectToSignallingServer: receiving candidates")
                    val candidate = IceCandidate(
                            message.getString("id"),
                            message.getInt("label"),
                            message.getString("candidate")
                    )
                    peerConnection?.addIceCandidate(candidate)
                }
            }
        }
    }

    private fun doCall() {
        started = true
        val sdpMediaConstraints = MediaConstraints()
//        sdpMediaConstraints.mandatory.add(
//                KeyValuePair("OfferToReceiveAudio", "true")
//        )
//        sdpMediaConstraints.mandatory.add(
//                KeyValuePair("OfferToReceiveVideo", "true")
//        )

//        sdpMediaConstraints.optional.add(KeyValuePair("DtlsSrtpKeyAgreement", "true"))
//        sdpMediaConstraints.optional.add(KeyValuePair("internalSctpDataChannels", "true"))
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                Log.d(TAG, "onCreateSuccess: ")
                peerConnection?.setLocalDescription(
                        object : SdpObserver {
                            override fun onCreateSuccess(p0: SessionDescription?) {}
                            override fun onSetSuccess() {}
                            override fun onCreateFailure(p0: String?) {}
                            override fun onSetFailure(p0: String?) {}
                        }, sessionDescription
                )
                val message = JSONObject()
                try {
                    message.put("type", "offer")
                    message.put("sdp", sessionDescription!!.description)
                    sendMessage(message)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, sdpMediaConstraints)
    }

    //MirtDPM4
    private fun doAnswer() {
        val sdpMediaConstraints = MediaConstraints()
//        sdpMediaConstraints.mandatory.add(
//                KeyValuePair("OfferToReceiveAudio", "true")
//        )
//        sdpMediaConstraints.mandatory.add(
//                KeyValuePair("OfferToReceiveVideo", "true")
//        )

//        sdpMediaConstraints.optional.add(KeyValuePair("DtlsSrtpKeyAgreement", "true"))
//        sdpMediaConstraints.optional.add(KeyValuePair("internalSctpDataChannels", "true"))
        peerConnection?.createAnswer(
                object : SdpObserver {
                    override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                        peerConnection?.setLocalDescription(object : SdpObserver {
                            override fun onCreateSuccess(p0: SessionDescription?) {}
                            override fun onSetSuccess() {}
                            override fun onCreateFailure(p0: String?) {}
                            override fun onSetFailure(p0: String?) {}
                        }, sessionDescription)
                        val message = JSONObject()
                        try {
                            message.put("type", "answer")
                            message.put("sdp", sessionDescription!!.description)
                            sendMessage(message)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onSetSuccess() {}
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, sdpMediaConstraints
        )
    }

    private fun sendMessage(message: JSONObject) {
        val contactTo = SPUtils[this, "contactTo", "contactUserName"] as String
        message.put("contactTo", contactTo)
        SocketIOHolder.send(message)
    }

    private fun initializeSurfaceViews() {
        rootEglBase = EglBase.create()
        binding.surfaceView.init(rootEglBase?.eglBaseContext, null)
        binding.surfaceView.setEnableHardwareScaler(true)
        binding.surfaceView.setMirror(true)

        binding.surfaceView2.init(rootEglBase?.eglBaseContext, null)
        binding.surfaceView2.setEnableHardwareScaler(true)
        binding.surfaceView2.setMirror(true)
    }

    private fun initializePeerConnectionFactory() {
        // 放到 MainApplication#onCreate
//        PeerConnectionFactory.initialize(
//            PeerConnectionFactory
//                .InitializationOptions
//                .builder(this)
//                .setEnableInternalTracer(true)
//                .createInitializationOptions()
//        )

        val encoderFactory: VideoEncoderFactory =
            DefaultVideoEncoderFactory(rootEglBase!!.eglBaseContext, true, true)
        val decoderFactory: VideoDecoderFactory =
            DefaultVideoDecoderFactory(rootEglBase!!.eglBaseContext)
        factory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()
    }

    private fun createVideoTrackFromCameraAndShowIt() {

        videoCapture = createVideoCapturer()
        // Create video source
        surfaceTextureHelper = SurfaceTextureHelper.create(
                "CaptureThread",
                rootEglBase?.eglBaseContext
        )
        videoSource = factory!!.createVideoSource(videoCapture!!.isScreencast)
        videoCapture?.initialize(surfaceTextureHelper, this, videoSource!!.capturerObserver)
        videoCapture?.startCapture(1280, 720, 30)

        // Create audio source
        audioSource = factory!!.createAudioSource(MediaConstraints())
    }

    private fun initializePeerConnections() {
        peerConnection = factory?.let { createPeerConnection(it) }
    }

    private fun createPeerConnection(factory: PeerConnectionFactory): PeerConnection? {
        val pcObserver: Observer =  object: Observer {
            override fun onSignalingChange(signalingState: SignalingState) {
                Log.d(TAG, "###################################### onSignalingChange: $signalingState")
            }

            override fun onIceConnectionChange(iceConnectionState: IceConnectionState) {
                Log.d(TAG, "onIceConnectionChange: ")
            }

            override fun onIceConnectionReceivingChange(b: Boolean) {
                Log.d(TAG, "onIceConnectionReceivingChange: ")
            }

            override fun onIceGatheringChange(iceGatheringState: IceGatheringState) {
                Log.d(TAG, "onIceGatheringChange: ")
            }

            override fun onIceCandidate(iceCandidate: IceCandidate) {
                Log.d(TAG, "onIceCandidate: ")
                val message = JSONObject()
                try {
                    message.put("type", "candidate")
                    message.put("label", iceCandidate.sdpMLineIndex)
                    message.put("id", iceCandidate.sdpMid)
                    message.put("candidate", iceCandidate.sdp)
                    Log.d(TAG, "onIceCandidate: sending candidate $message")
                    sendMessage(message)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
                Log.d(TAG, "onIceCandidatesRemoved: ")
            }

            override fun onAddStream(mediaStream: MediaStream) {
                Log.d(TAG, "onAddStream: " + mediaStream.videoTracks.size)
            }

            override fun onRemoveStream(mediaStream: MediaStream) {
                Log.d(TAG, "onRemoveStream: ")
            }

            override fun onDataChannel(dc: DataChannel) {
                Log.d(TAG, "onDataChannel: ")
                dataChannel = dc
                dataChannel?.registerObserver(object : DataChannel.Observer {
                    override fun onBufferedAmountChange(p0: Long) {}

                    override fun onStateChange() {}

                    override fun onMessage(buffer: DataChannel.Buffer?) {
                        Log.d(TAG, "********** 接收到消息 ***********")
                        val data = buffer!!.data
                        val bytes = ByteArray(data.capacity())
//                        data.get(bytes)
                        data[bytes]
                        val strData = String(bytes)
                        runOnUiThread {
                            Toast.makeText(this@WebRtcActivity, strData, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }

            override fun onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded: ")
                if(started) doCall()
            }

            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
                if(receiver?.track() == null) {
                    return
                }
                if(receiver.track() is VideoTrack) {
                    val remoteVideoTrack: VideoTrack = receiver.track() as VideoTrack
                    remoteVideoTrack.setEnabled(true)
                    remoteVideoTrack.addSink(binding.surfaceView2)
                } else if(receiver.track() is AudioTrack) {
                    val remoteAudioTrack = receiver.track() as AudioTrack
                    remoteAudioTrack.setEnabled(true)
                }
            }
        }

        val iceServers = ArrayList<IceServer>()
        val stunPeerIceServer: IceServer = IceServer.builder("stun:49.232.162.58:3478").createIceServer()
        iceServers.add(stunPeerIceServer)
//        val turnPeerIceServer: IceServer = IceServer.builder("turn:49.232.162.58:3478")
//                .setUsername("aaaaa").setPassword("bbbbb")
//                .createIceServer()
//        iceServers.add(turnPeerIceServer)

//        iceServers.add(IceServer("stun:49.232.162.58:3478"))
//        iceServers.add(IceServer("turn:49.232.162.58:3478", "aaaaa", "bbbbb"))
        val rtcConfig = RTCConfiguration(iceServers)
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = TcpCandidatePolicy.DISABLED
        rtcConfig.bundlePolicy = BundlePolicy.MAXBUNDLE
        rtcConfig.rtcpMuxPolicy = RtcpMuxPolicy.REQUIRE
        rtcConfig.continualGatheringPolicy = ContinualGatheringPolicy.GATHER_CONTINUALLY
        // Use ECDSA encryption.
        rtcConfig.keyType = KeyType.ECDSA
        // Enable DTLS for normal calls and disable for loopback calls.
        rtcConfig.enableDtlsSrtp = true
        rtcConfig.sdpSemantics = SdpSemantics.UNIFIED_PLAN

//        rtcConfig.enableRtpDataChannel = true

        return factory.createPeerConnection(rtcConfig, pcObserver)
    }


    val VIDEO_TRACK_ID = "ARDAMSv0"
    val AUDIO_TRACK_ID = "101"

    private fun startStreamingVideo() {
        // Create video track
        val videoTrack: VideoTrack = factory!!.createVideoTrack(VIDEO_TRACK_ID, videoSource)
        videoTrack.setEnabled(true)
        videoTrack.addSink(binding.surfaceView)

        // Create audio track
        val audioTrack: AudioTrack = factory!!.createAudioTrack(AUDIO_TRACK_ID, audioSource)
        audioTrack.setEnabled(true)

//        val mediaStream = factory!!.createLocalMediaStream("ARDAMS")
//        mediaStream.addTrack(videoTrack)
//        mediaStream.addTrack(audioTrack)

        peerConnection!!.addTrack(videoTrack)
        peerConnection!!.addTrack(audioTrack)
//        peerConnection!!.addStream(mediaStream)
//        sendMessage("got user media")
    }


    private fun createVideoCapturer(): VideoCapturer? {
        return if (useCamera2()) {
            createCameraCapturer(Camera2Enumerator(this))
        } else {
            createCameraCapturer(Camera1Enumerator(true))
        }
    }

    private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }

    private fun useCamera2(): Boolean {
        return Camera2Enumerator.isSupported(this)
    }
    
}