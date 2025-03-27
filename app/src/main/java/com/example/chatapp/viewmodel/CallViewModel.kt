package com.example.chatapp.viewmodel

import android.util.Log
import android.view.SurfaceView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.AGORA_APP_ID
import com.example.chatapp.repository.AgoraSetUpRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.video.VideoCanvas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val agoraRepo: AgoraSetUpRepo
) : ViewModel() {


    val isJoined: StateFlow<Boolean> =  agoraRepo.isJoined
    val remoteUserJoined: StateFlow<Int?> = agoraRepo.remoteUserJoined
    val remoteUserLeft: StateFlow<Boolean> = agoraRepo.remoteUserLeft

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> get() = _isMuted

    private val _isSpeakerEnabled = MutableStateFlow(false)
    val isSpeakerEnabled: StateFlow<Boolean> get() = _isSpeakerEnabled

    private val _callEnded = MutableStateFlow(false)
    val callEnded: StateFlow<Boolean> get() = _callEnded

    fun joinChannel(token: String?, channelId: String, uid: Int) {
        viewModelScope.launch {
            agoraRepo.joinChannel(token, channelId, uid)
        }
    }


    init {
        agoraRepo.initializeAgora(AGORA_APP_ID)
    }

    fun leaveChannel() {
        viewModelScope.launch {
            agoraRepo.leaveChannel()
            _callEnded.value = true
        }

    }

    fun setUpLocalVideo(surfaceView: SurfaceView)
    {
        agoraRepo.setupLocalVideo(surfaceView)
    }

    fun setUpRemoteVideo(surfaceView: SurfaceView, uid: Int)
    {
        agoraRepo.setupRemoteVideo(surfaceView,uid)
    }

    fun muteAudio() {
        _isMuted.value = !_isMuted.value
        agoraRepo.muteAudio(_isMuted.value)
    }

    fun toggleSpeaker() {
        _isSpeakerEnabled.value = !_isSpeakerEnabled.value
        agoraRepo.enableSpeaker(_isSpeakerEnabled.value)
    }

    fun switchCamera() {
        agoraRepo.switchCamera()
    }

    override fun onCleared() {
        super.onCleared()
        agoraRepo.destroy()
    }
}