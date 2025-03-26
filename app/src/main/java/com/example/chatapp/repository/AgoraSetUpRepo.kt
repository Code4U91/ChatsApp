package com.example.chatapp.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
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
          //  rtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            rtcEngine?.enableVideo()
        //    rtcEngine?.enableAudio()
           // rtcEngine?.setDefaultAudioRoutetoSpeakerphone(true)
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

    fun startLocalVideo(surfaceView: VideoCanvas)
    {
        rtcEngine?.setupLocalVideo(surfaceView)
        rtcEngine?.muteLocalVideoStream(false)
        rtcEngine?.muteLocalAudioStream(false)
        rtcEngine?.startPreview()

    }

    fun joinChannel(
        token: String?,
        channelName: String
    )
    {
        rtcEngine?.joinChannel(token,channelName, "", 0)
      //  rtcEngine?.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY, Constants.AUDIO_SCENARIO_CHATROOM)

    }

    fun leaveChannel()
    {
        rtcEngine?.leaveChannel()
    }

    fun setUpRemoteVideo(uid: Int, remoteVideoCanvas: VideoCanvas)
    {
        rtcEngine?.setupRemoteVideo(remoteVideoCanvas)
        rtcEngine?.muteRemoteAudioStream(uid, false)
    }

    fun destroy()
    {
        rtcEngine?.let {
            RtcEngine.destroy()
            rtcEngine = null
        }
    }
}