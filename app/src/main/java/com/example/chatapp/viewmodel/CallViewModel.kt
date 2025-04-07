package com.example.chatapp.viewmodel

import android.view.SurfaceView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.AGORA_ID
import com.example.chatapp.CALL_HISTORY
import com.example.chatapp.repository.AgoraSetUpRepo
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val agoraRepo: AgoraSetUpRepo,
    private val firestoreDb: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {


    val isJoined: StateFlow<Boolean> = agoraRepo.isJoined
    val remoteUserJoined: StateFlow<Int?> = agoraRepo.remoteUserJoined
    val remoteUserLeft: StateFlow<Boolean> = agoraRepo.remoteUserLeft

    private val _isMuted = MutableStateFlow(false)
    val isMuted = _isMuted.asStateFlow()

    private val _isRemoteAudioDeafen = MutableStateFlow(false) // changed to true
    val isRemoteAudioDeafen = _isRemoteAudioDeafen.asStateFlow()

    private val _isSpeakerPhoneEnabled = MutableStateFlow(false) // changed to true
    val isSpeakerPhoneEnabled = _isSpeakerPhoneEnabled.asStateFlow()

    private val _callEnded = MutableStateFlow(false)
    val callEnded = _callEnded.asStateFlow()

    private val _callStartTime = MutableStateFlow<Long?>(null)
    private val _callDuration = MutableStateFlow(0L)

    private val _isCaller = MutableStateFlow(true)


    val callDuration = _callDuration.asStateFlow()

    private var callId: String? = null

    private var callTimerJob: Job? = null


    init {

        // for testing purpose only

        agoraRepo.initializeAgora(AGORA_ID)

        viewModelScope.launch {
            remoteUserJoined.collect { userId ->

                if (userId != null) {
                    startCallTimer()
                }

            }
        }

        viewModelScope.launch {
            remoteUserLeft.collect { isLeft ->

                if (isLeft) {
                    stopCallTimer()
                }
            }
        }
    }

    fun updateIsCaller(state: Boolean) {
        if (_isCaller.value != state) {
            _isCaller.value = state
        }
    }

    fun joinChannel(token: String?, channelId: String, callType: String) {

        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            agoraRepo.joinChannel(token, channelId, callType, userId)
        }

    }

    fun leaveChannel() {
        _callEnded.value = true
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

    private fun startCallTimer() {
        _callStartTime.value = System.currentTimeMillis()

        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {

            while (isActive) {
                val durationMillis = System.currentTimeMillis() - (_callStartTime.value ?: 0L)
                _callDuration.value = durationMillis / 1000 // converted to seconds
                delay(1000)
            }
        }
    }

    private fun stopCallTimer() {

        callTimerJob?.cancel()
        _callStartTime.value?.let { startTime ->
            val durationMillis = System.currentTimeMillis() - startTime
            _callDuration.value = durationMillis / 1000
        }

        _callStartTime.value = null

    }

    fun enableVideoPreview() {
        agoraRepo.enableVideo()
    }

    fun uploadCallData(
        callReceiverId: String?,
        callType: String,
        channelId: String,
        callStatus: String,
        callerName: String,
        receiverName: String
    ) {
        val userId = auth.currentUser?.uid ?: return

        callReceiverId?.let { receiverId ->

            val callDocRef = firestoreDb.collection(CALL_HISTORY).document()

            val mapIdWithName = mapOf(
                userId to callerName,
                receiverId to receiverName
            )

            val callData = mapOf(
                "callerId" to userId,
                "callReceiverId" to receiverId,
                "callType" to callType,
                "channelId" to channelId,
                "status" to callStatus,
                "callStartTime" to Timestamp.now(),
                "participants" to listOf(userId, receiverId),
                "participantsName" to mapIdWithName
            )

            callDocRef.set(callData)
            callId = callDocRef.id
        }

    }

    fun updateCallStatus(status: String) {
        callId?.let { id ->
            val callDocRef = firestoreDb.collection(CALL_HISTORY).document(id)

            callDocRef.get().addOnSuccessListener { doc ->

                if (doc.exists()) {
                    val newStatus = mapOf(
                        "status" to status,
                    )

                    callDocRef.update(newStatus)
                }

            }
        }


    }

    override fun onCleared() {

        if (_isCaller.value) {

            callId?.let { id ->

                val callDocRef = firestoreDb.collection(CALL_HISTORY).document(id)
                val status = if (remoteUserJoined.value != null) "ended" else "missed"

                val callEndTimeData = mapOf(
                    "callEndTime" to Timestamp.now(),
                    "status" to status
                )
                callDocRef.update(callEndTimeData)

            }
        }


        agoraRepo.destroy()
        super.onCleared()


    }


}