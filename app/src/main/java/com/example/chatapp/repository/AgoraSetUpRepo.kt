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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AgoraSetUpRepo @Inject constructor(
    @ApplicationContext private val context: Context
) {


    private var rtcEngine: RtcEngine? = null

    private val _isJoined = MutableStateFlow(false)
    val isJoined = _isJoined.asStateFlow()

    private val _remoteUserJoined = MutableStateFlow<Int?>(null)
    val remoteUserJoined = _remoteUserJoined.asStateFlow()

    private val _remoteUserLeft = MutableStateFlow(false)
    val remoteUserLeft = _remoteUserLeft.asStateFlow()

    private var localUid: Int = 0

    fun initializeAgora(appId: String) {
        try {
            rtcEngine = RtcEngine.create(context, appId, object : IRtcEngineEventHandler() {

                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    super.onJoinChannelSuccess(channel, uid, elapsed)

                    localUid = uid
                    _isJoined.value = true
                }

                override fun onUserJoined(uid: Int, elapsed: Int) {
                    Log.d("AgoraDebug", "Remote user joined: $uid")
                    _remoteUserJoined.value = uid

                }

                override fun onLocalUserRegistered(uid: Int, userAccount: String?) {
                    super.onLocalUserRegistered(uid, userAccount)

                    localUid = uid

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
            startPreview() //  preview to run before call join
        }
    }

    private fun enableAudioOnly() {
        rtcEngine?.apply {
            disableVideo()
            setEnableSpeakerphone(false)
        }
    }

    fun toggleSpeakerphone(isEnabled: Boolean) {
        rtcEngine?.setEnableSpeakerphone(isEnabled)
    }

    suspend fun joinChannel(token: String?, channelName: String, callType: String, uid: String) {

        withContext(Dispatchers.IO)
        {
            val options = ChannelMediaOptions().apply {
                clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
                publishMicrophoneTrack = true

                if (callType == "video") {
                    publishCameraTrack = true
                    enableVideo()
                } else {
                    publishCameraTrack = false
                    enableAudioOnly()
                }

            }

            // rtcEngine?.joinChannel(token, channelName, 0, options)
            rtcEngine?.joinChannelWithUserAccount(token, channelName, uid, options)
        }

    }

    fun leaveChannel() {

        rtcEngine?.apply {
            leaveChannel()
            disableVideo()
            muteAllRemoteAudioStreams(false)
        }
        _isJoined.value = false
        _remoteUserJoined.value = null
    }

    fun setupLocalVideo(surfaceView: SurfaceView) {
        rtcEngine?.setupLocalVideo(
            VideoCanvas(
                surfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                localUid
            )
        )

    }

    fun setupRemoteVideo(surfaceView: SurfaceView, uid: Int) {
        rtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
    }

    fun switchCamera() {
        rtcEngine?.switchCamera()
    }

    fun muteLocalAudio(mute: Boolean) {
        rtcEngine?.muteLocalAudioStream(mute)
    }

    fun muteRemoteAudio(enabled: Boolean) {

        rtcEngine?.muteAllRemoteAudioStreams(enabled)

    }


    fun destroy() {

        rtcEngine?.apply {
            leaveChannel()
            stopPreview()
            disableVideo()
            disableAudio()
            setEnableSpeakerphone(true)
        }

        RtcEngine.destroy()
        rtcEngine = null

        Log.i("AgoraDebug", "Called On Destroy")


    }

}