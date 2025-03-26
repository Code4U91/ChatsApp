package com.example.chatapp.repository

import android.content.Context
import android.view.SurfaceView
import dagger.hilt.android.qualifiers.ApplicationContext
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import javax.inject.Inject

class AgoraSetUpRepo @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var rtcEngine: RtcEngine? = null



    fun initializeAgoraEngine(
        appId: String,
        eventHandler: IRtcEngineEventHandler)
    {
        if (rtcEngine == null)
        {
            rtcEngine = RtcEngine.create(context, appId, eventHandler)

        }

    }

    private fun enableVideo() {
        rtcEngine?.apply {
            enableVideo()
            startPreview()
        }
    }


    fun toggleSpeaker(enable: Boolean) {
        rtcEngine?.setDefaultAudioRoutetoSpeakerphone(enable)
        rtcEngine?.setEnableSpeakerphone(enable)
    }

    fun setSpeakerphoneOn(enable: Boolean) {
        rtcEngine?.setEnableSpeakerphone(enable)
    }

    fun muteLocalAudioStream(mute: Boolean)
    {
        rtcEngine?.muteLocalAudioStream(mute)
    }

    fun muteLocalVideoStream(mute: Boolean)
    {
        rtcEngine?.muteLocalVideoStream(mute)
    }

    fun startLocalVideo(surfaceView: SurfaceView)
    {
        rtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
        //rtcEngine?.startPreview()

    }

    fun joinChannel(channelName: String) {
        val options = ChannelMediaOptions().apply {
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            publishMicrophoneTrack = true
            publishCameraTrack = true
        }
        rtcEngine?.joinChannel(null, channelName, 0, options)
        enableVideo()
    }

    fun leaveChannel()
    {
        rtcEngine?.leaveChannel()
    }

    fun setUpRemoteVideo(uid: Int, remoteVideoCanvas: VideoCanvas)
    {
        rtcEngine?.setupRemoteVideo(remoteVideoCanvas)
       // rtcEngine?.muteRemoteAudioStream(uid, false)
    }

    fun destroy()
    {
        rtcEngine?.let {
            RtcEngine.destroy()
            rtcEngine = null
        }
    }
}