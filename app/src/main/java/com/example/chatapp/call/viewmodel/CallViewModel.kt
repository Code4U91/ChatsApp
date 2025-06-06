package com.example.chatapp.call.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.SurfaceView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.CallMetadata
import com.example.chatapp.call.repository.AgoraSetUpRepo
import com.example.chatapp.call.repository.CallSessionUpdaterRepo
import com.example.chatapp.call.service.AgoraCallService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val agoraRepo: AgoraSetUpRepo,
    private val auth: FirebaseAuth,
    private var callSessionUpdaterRepo: CallSessionUpdaterRepo
) : ViewModel() {


    val isJoined: StateFlow<Boolean> = agoraRepo.isJoined
    val remoteUserJoined: StateFlow<Int?> = agoraRepo.remoteUserJoined
    val remoteUserLeft: StateFlow<Boolean> = agoraRepo.remoteUserLeft
    private val declineCall: StateFlow<Boolean> = agoraRepo.declineTheCall


    private val _isMuted = MutableStateFlow(false)
    val isMuted = _isMuted.asStateFlow()

    private val _isRemoteAudioDeafen = MutableStateFlow(false) // changed to true
    val isRemoteAudioDeafen = _isRemoteAudioDeafen.asStateFlow()

    private val _isSpeakerPhoneEnabled = MutableStateFlow(false) // changed to true
    val isSpeakerPhoneEnabled = _isSpeakerPhoneEnabled.asStateFlow()

    private val _callEnded = MutableStateFlow(false)
    val callEnded = _callEnded.asStateFlow()

    private val _callScreenData = MutableStateFlow<CallMetadata?>(null)
    val callScreenData = _callScreenData.asStateFlow()

    private val _hasStartedCallService = MutableStateFlow(false)
    val hasStartedCallService = _hasStartedCallService.asStateFlow()


    val callDuration = agoraRepo.callDuration


    init {


        // when the outgoing call gets declined, used to close the call
        val job = viewModelScope.launch {
            declineCall.collect { decline ->
                if (decline && !_callEnded.value) {
                    _callEnded.value = true
                }
            }
        }


        viewModelScope.launch {
            remoteUserJoined.collect { userId ->

                if (userId != null) {
                    job.cancel()
                }

            }
        }

        viewModelScope.launch {
            remoteUserLeft.collect { isLeft ->

                if (isLeft) {
                    _callEnded.value = true // needed otherwise causes re calling when call ends
                }
            }
        }

        viewModelScope.launch {

            agoraRepo.isJoined.collect{isJoined ->
                if(isJoined)
                {
                    markCallServiceStarted()
                } else {
                    resetCallServiceFlag()
                }
            }
        }

    }

    fun markCallServiceStarted() {

        if (!_hasStartedCallService.value) _hasStartedCallService.value = true

    }

    fun resetCallServiceFlag() {
        if (_hasStartedCallService.value) _hasStartedCallService.value = false
    }

    fun setCallScreenData(data: CallMetadata?)
    {
        _callScreenData.value = data
    }

    @SuppressLint("ObsoleteSdkInt")
    fun startCallService(
        context: Context,
        channelName: String,
        callType: String,
        callReceiverId: String,
        callerName: String,
        receiverName: String,
        isCaller: Boolean,
        photo: String,
        callDocId: String?
    ) {
        val userId = auth.currentUser?.uid ?: return

        val callMetadata = CallMetadata(
            channelName = channelName,
            uid = userId,
            callType = callType,
            callerName = callerName,
            receiverName = receiverName,
            isCaller = isCaller,
            callReceiverId = callReceiverId,
            receiverPhoto = photo,
            callDocId = callDocId
        )

        Log.i("METADATA_CALL_VIEWMODEL", callMetadata.toString())

        val intent = Intent(context, AgoraCallService::class.java).apply {

            putExtra("call_metadata", callMetadata)


        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

    }

    fun stopCallService(context: Context) {
        context.stopService(Intent(context, AgoraCallService::class.java))
    }


    fun leaveChannel() {
        _callEnded.value = true
    }

    fun declineTheCall(decline: Boolean, callDocId: String) {

        callSessionUpdaterRepo.updateCallStatus("declined", callDocId)
        agoraRepo.declineIncomingCall(decline)
    }

    fun setUpLocalVideo(surfaceView: SurfaceView) {
        agoraRepo.setupLocalVideo(surfaceView)
    }

    fun setUpRemoteVideo(surfaceView: SurfaceView, uid: Int) {
        agoraRepo.setupRemoteVideo(surfaceView, uid)
    }

    fun muteOutgoingAudio() {
        _isMuted.value = !_isMuted.value
        agoraRepo.muteLocalAudio(_isMuted.value)
    }

    fun muteYourSpeaker() {
        _isRemoteAudioDeafen.value = !_isRemoteAudioDeafen.value
        agoraRepo.muteRemoteAudio(_isRemoteAudioDeafen.value)
    }

    fun switchCamera() {
        agoraRepo.switchCamera()
    }

    fun toggleSpeaker() {
        _isSpeakerPhoneEnabled.value = !_isSpeakerPhoneEnabled.value
        agoraRepo.toggleSpeakerphone(_isSpeakerPhoneEnabled.value)
    }


    fun enableVideoPreview() {
        agoraRepo.enableVideo()
    }

    override fun onCleared() {
        super.onCleared()

        // needed to reset the value when agoraCallService is not active
        // as the default value reset only works on destruction of the agoraCallService and
        // call service only activates if the user accepts the call or is a caller
        agoraRepo.declineIncomingCall(false)
    }


}