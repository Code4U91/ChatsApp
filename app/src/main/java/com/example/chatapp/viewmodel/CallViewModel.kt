package com.example.chatapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.chatapp.AGORA_APP_ID
import com.example.chatapp.repository.AgoraSetUpRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.video.VideoCanvas
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val agoraRepo: AgoraSetUpRepo
) : ViewModel() {

    private var onRemoteUserJoined: ((Int) -> Unit)? = null

    fun initializeAgora()
    {
        agoraRepo.initializeAgoraEngine(AGORA_APP_ID, rtcEventHandler)
    }

    fun startLocalVideo(surfaceView: VideoCanvas)
    {
        agoraRepo.startLocalVideo(surfaceView)
    }

    fun joinChannel(token: String?, channelName: String)
    {
        agoraRepo.joinChannel(token, channelName)

    }

    fun leaveChannel(){
        agoraRepo.leaveChannel()
    }

    fun setUpRemoteVideo(uid: Int, remoteVideoCanvas: VideoCanvas)
    {
        agoraRepo.setUpRemoteVideo(uid,remoteVideoCanvas)

    }

    fun setRemoteVideoListener(listener: (Int) -> Unit) {
        onRemoteUserJoined =  listener
    }

    fun toggleSpeaker(enable: Boolean) {
       agoraRepo.toggleSpeaker(enable)
    }


    fun muteLocalAudio(mute: Boolean)
    {
        agoraRepo.muteLocalAudioStream(mute)
    }

    fun muteLocalVideo(mute: Boolean)
    {
        agoraRepo.muteLocalVideoStream(mute)
    }


    fun callEnd()
    {
        agoraRepo.destroy()
    }



    private val rtcEventHandler = object : IRtcEngineEventHandler()
    {
//        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
//            super.onJoinChannelSuccess(channel, uid, elapsed)
//            Log.i("AgoraDebug", "Joined channel: $channel with uid: $uid")
//        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            Log.i("AgoraDebug", "Remote user joined: $uid")
            onRemoteUserJoined?.invoke(uid)
        }

//        override fun onUserOffline(uid: Int, reason: Int) {
//            super.onUserOffline(uid, reason)
//            callEnd()
//            Log.i("AgoraDebug", "User offline: $uid Reason: $reason")
//        }

//        @Deprecated("Deprecated in Java")
//        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
//            Log.i("AgoraDebug", "First remote video decoded: $uid")
//            onRemoteUserJoined?.invoke(uid)
//        }



//        override fun onRemoteVideoStateChanged(
//            uid: Int,
//            state: Int,
//            reason: Int,
//            elapsed: Int
//        ) {
//            super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
//            if (state == Constants.REMOTE_VIDEO_STATE_DECODING && reason == Constants.REMOTE_VIDEO_STATE_REASON_REMOTE_UNMUTED) {
//                Log.i("AgoraDebug", "Remote video decoded for UID: $uid")
//                if (state == Constants.REMOTE_VIDEO_STATE_DECODING) {
//                    onRemoteUserJoined?.invoke(uid)
//                }
//            }
//        }


//        override fun onUserEnableVideo(uid: Int, enabled: Boolean) {
//            super.onUserEnableVideo(uid, enabled)
//            Log.i("AgoraDebug", "User $uid video enabled: $enabled")
//            if (enabled) {
//                onRemoteUserJoined?.invoke(uid)
//            }
//        }

    }
}