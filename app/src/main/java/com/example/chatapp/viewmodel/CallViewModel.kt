package com.example.chatapp.viewmodel

import android.view.SurfaceView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.AGORA_APP_ID
import com.example.chatapp.repository.AgoraSetUpRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val isMuted = _isMuted.asStateFlow()

    private val _isRemoteAudioDeafen = MutableStateFlow(false) // changed to true
    val isRemoteAudioDeafen  = _isRemoteAudioDeafen.asStateFlow()

    private val _isSpeakerPhoneEnabled = MutableStateFlow(false) // changed to true
    val isSpeakerPhoneEnabled  = _isSpeakerPhoneEnabled.asStateFlow()

    private val _callEnded = MutableStateFlow(false)
    val callEnded = _callEnded.asStateFlow()



    init {
        agoraRepo.initializeAgora(AGORA_APP_ID)
    }


    fun joinChannel(token: String?, channelId: String, uid: Int, callType: String) {
        viewModelScope.launch {
            agoraRepo.joinChannel(token, channelId, callType)
        }
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

    fun muteOutgoingAudio() {
        _isMuted.value = !_isMuted.value
        agoraRepo.muteLocalAudio(_isMuted.value)
    }

    fun  muteYourSpeaker() {
        _isRemoteAudioDeafen.value = !_isRemoteAudioDeafen.value
        agoraRepo.muteRemoteAudio(_isRemoteAudioDeafen.value)
    }

    fun switchCamera() {
        agoraRepo.switchCamera()
    }

    fun toggleSpeaker()
    {
        _isSpeakerPhoneEnabled.value = !_isSpeakerPhoneEnabled.value
        agoraRepo.toggleSpeakerphone(_isSpeakerPhoneEnabled.value)
    }

    override fun onCleared() {
        super.onCleared()
        agoraRepo.destroy()
    }

    fun enableVideoPreview() {
         agoraRepo.enableVideo()
    }
}