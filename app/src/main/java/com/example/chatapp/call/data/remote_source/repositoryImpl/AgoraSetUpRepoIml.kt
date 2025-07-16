package com.example.chatapp.call.data.remote_source.repositoryImpl

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import com.example.chatapp.call.domain.repository.AgoraSetUpRepo
import com.example.chatapp.call.presentation.call_screen.state.CallEvent
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

// Contains all function needed to make and receive call
// used by AgoraCallService class for running it in the service class
class AgoraSetUpRepoIml (
    private val context: Context
) : AgoraSetUpRepo {

    private var rtcEngine: RtcEngine? = null

    private val _isJoined = MutableStateFlow(false)
    override val isJoined = _isJoined.asStateFlow()

    private val _remoteUserJoined = MutableStateFlow<Int?>(null)
    override val remoteUserJoined = _remoteUserJoined.asStateFlow()

    private val _remoteUserLeft = MutableStateFlow(false)
    override val remoteUserLeft = _remoteUserLeft.asStateFlow()

    private val _declineTheCall = MutableStateFlow(false)
    override val declineTheCall = _declineTheCall.asStateFlow()

    private val _callDuration = MutableStateFlow(0L)
    override val callDuration = _callDuration.asStateFlow()

    private val _callEvent = MutableStateFlow<CallEvent>(CallEvent.JoiningChannel)
    override val callEvent = _callEvent.asStateFlow()


    private var localUid: Int = 0

    override fun initializeAgora(appId: String) {

        if (rtcEngine != null) return

        try {
            rtcEngine = RtcEngine.create(context, appId, object : IRtcEngineEventHandler() {

                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    super.onJoinChannelSuccess(channel, uid, elapsed)

                    localUid = uid
                    _isJoined.value = true

                    updateCallEvent(CallEvent.Ringing)

                }

                override fun onLeaveChannel(stats: RtcStats?) {
                    super.onLeaveChannel(stats)
                    _isJoined.value = false

                    updateCallEvent(CallEvent.Ended)
                }

                override fun onUserJoined(uid: Int, elapsed: Int) {
                    Log.d("AgoraDebug", "Remote user joined: $uid")
                    _remoteUserJoined.value = uid

                    updateCallEvent(CallEvent.Ongoing)

                }

                override fun onLocalUserRegistered(uid: Int, userAccount: String?) {
                    super.onLocalUserRegistered(uid, userAccount)

                    localUid = uid

                }

                override fun onUserOffline(uid: Int, reason: Int) {
                    Log.d("AgoraDebug", "Remote user left: $uid")

                    _remoteUserLeft.value = true

                    updateCallEvent(CallEvent.Ended)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun enableVideo() {
        rtcEngine?.apply {
            enableVideo()
            startPreview() //  preview to run before call join
            setEnableSpeakerphone(true)
        }
    }

    override fun enableAudioOnly() {
        rtcEngine?.apply {
            disableVideo()
            setEnableSpeakerphone(false)
        }
    }

    override fun toggleSpeakerphone(isEnabled: Boolean) {
        rtcEngine?.setEnableSpeakerphone(isEnabled)
    }

    override suspend fun joinChannel(token: String?, channelName: String, callType: String, uid: String) {


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
            rtcEngine?.joinChannelWithUserAccount(token, channelName, uid, options)
        }

    }


    override fun setupLocalVideo(surfaceView: SurfaceView) {

        rtcEngine?.apply {
            enableVideo()
            setupLocalVideo(
                VideoCanvas(
                    surfaceView,
                    VideoCanvas.RENDER_MODE_HIDDEN,
                    localUid
                )
            )
            startPreview()
        }

    }


    override fun setupRemoteVideo(surfaceView: SurfaceView, uid: Int) {
        rtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
    }

    override fun switchCamera() {
        rtcEngine?.switchCamera()
    }

    override fun muteLocalAudio(mute: Boolean) {
        rtcEngine?.muteLocalAudioStream(mute)
    }

    override fun muteRemoteAudio(enabled: Boolean) {

        rtcEngine?.muteAllRemoteAudioStreams(enabled)

    }

    override fun declineIncomingCall(decline: Boolean) {

        _declineTheCall.value = decline
    }

    override fun updateDuration(duration: Long) {
        _callDuration.value = duration
    }

    override fun resetCallDuration() {
        _callDuration.value = 0L
    }

    override fun updateCallEvent(event: CallEvent) {

        if(_callEvent.value != event){
            _callEvent.update { event }
        }
    }


    override fun destroy() {

        rtcEngine?.apply {
            leaveChannel()
            stopPreview()
            disableVideo()
            disableAudio()
            setEnableSpeakerphone(true)
        }

        // important to reset these or else will interfere with call screen callEnd and close the call as soon as user joined after the second
        // calls since app start
        _isJoined.value = false
        _remoteUserLeft.value = false
        _remoteUserJoined.value = null
        _declineTheCall.value = false


         resetCallDuration()



        RtcEngine.destroy()
        rtcEngine = null

        Log.i("AgoraDebug", "Called On Destroy")


    }

}