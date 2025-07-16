package com.example.chatapp.call.domain.repository

import android.view.SurfaceView
import com.example.chatapp.call.presentation.call_screen.state.CallEvent
import kotlinx.coroutines.flow.StateFlow

interface AgoraSetUpRepo {

    val isJoined : StateFlow<Boolean>
    val remoteUserJoined : StateFlow<Int?>
    val remoteUserLeft : StateFlow<Boolean>
    val declineTheCall : StateFlow<Boolean>
    val callDuration: StateFlow<Long>
    val callEvent  : StateFlow<CallEvent>


    fun initializeAgora(appId: String)

    fun enableVideo()

    fun enableAudioOnly()

    fun toggleSpeakerphone(isEnabled: Boolean)

    suspend fun joinChannel(token: String?, channelName: String, callType: String, uid: String)
    fun setupLocalVideo(surfaceView: SurfaceView)
    fun setupRemoteVideo(surfaceView: SurfaceView, uid: Int)

    fun switchCamera()

    fun muteLocalAudio(mute: Boolean)

    fun muteRemoteAudio(enabled: Boolean)

    fun declineIncomingCall(decline: Boolean)

    fun updateDuration(duration: Long)

    fun resetCallDuration()

    fun updateCallEvent(event: CallEvent)


    fun destroy()

}