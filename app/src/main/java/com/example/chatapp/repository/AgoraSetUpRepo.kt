package com.example.chatapp.repository

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import dagger.hilt.android.qualifiers.ApplicationContext
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class AgoraSetUpRepo @Inject constructor(
    @ApplicationContext private val context: Context
) {


    private var rtcEngine: RtcEngine? = null

    private val _isJoined = MutableStateFlow(false)
    val isJoined: StateFlow<Boolean> get() = _isJoined

    private val _remoteUserJoined = MutableStateFlow<Int?>(null)
    val remoteUserJoined: StateFlow<Int?> get() = _remoteUserJoined

    private val _remoteUserLeft = MutableStateFlow(false)
    val remoteUserLeft: StateFlow<Boolean> get() = _remoteUserLeft

    fun initializeAgora(appId: String) {
        try {
            rtcEngine = RtcEngine.create(context, appId, object : IRtcEngineEventHandler() {

                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    super.onJoinChannelSuccess(channel, uid, elapsed)

                    _isJoined.value = true
                }
                override fun onUserJoined(uid: Int, elapsed: Int) {
                    Log.d("AgoraDebug", "Remote user joined: $uid")
                    _remoteUserJoined.value = uid
                    enableVideo()
                }

                override fun onUserOffline(uid: Int, reason: Int) {
                    _remoteUserLeft.value = true
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun enableVideo() {
        rtcEngine?.apply {
            enableVideo()
            startPreview()
        }
    }

    fun joinChannel(token: String?, channelName: String, uid: Int) {

        val options = ChannelMediaOptions().apply {
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            publishMicrophoneTrack = true
            publishCameraTrack = true
        }

        rtcEngine?.joinChannel(token, channelName, 0, options)
    }

    fun leaveChannel() {
        rtcEngine?.leaveChannel()
        _isJoined.value = false
        _remoteUserJoined.value =  null
    }

    fun setupLocalVideo(surfaceView: SurfaceView) {
        rtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
    }

    fun setupRemoteVideo(surfaceView: SurfaceView, uid: Int) {
        rtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
    }

    fun switchCamera() {
        rtcEngine?.switchCamera()
    }

    fun muteAudio(mute: Boolean) {
        rtcEngine?.muteLocalAudioStream(mute)
    }

    fun enableSpeaker(enabled: Boolean) {
        rtcEngine?.setEnableSpeakerphone(enabled)
    }

    fun destroy() {
        rtcEngine?.apply {
            leaveChannel()  // Step 1: Leave the channel
            stopPreview()   // Step 2: Stop camera preview (if started)
            disableVideo()  // Step 3: Disable video
            disableAudio()  // Step 3: Disable audio
        }

        RtcEngine.destroy() // Step 4: Destroy the global instance
        rtcEngine = null   // Step 5: Release the reference

        Log.i("AgoraDebug", "Called On Destroy")
    }

}