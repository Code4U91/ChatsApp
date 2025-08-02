package com.example.chatapp.call_feature.presentation.call_screen.viewmodel

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.call_feature.domain.repository.AgoraSetUpRepo
import com.example.chatapp.call_feature.domain.usecase.audio_case.CallAudioCase
import com.example.chatapp.call_feature.domain.usecase.call_case.CallUseCase
import com.example.chatapp.call_feature.domain.usecase.call_video_case.CallVideoCase
import com.example.chatapp.call_feature.presentation.call_screen.state.CallEvent
import com.example.chatapp.call_feature.presentation.call_screen.state.CallUIState
import com.example.chatapp.core.model.CallMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val agoraRepo: AgoraSetUpRepo,
    private val callUseCase: CallUseCase,
    private val audioUseCase: CallAudioCase,
    private val videoUseCase: CallVideoCase
) : ViewModel() {


    val isJoined: StateFlow<Boolean> = agoraRepo.isJoined
    val remoteUserJoined: StateFlow<Int?> = agoraRepo.remoteUserJoined
    val remoteUserLeft: StateFlow<Boolean> = agoraRepo.remoteUserLeft
    private val declineCall: StateFlow<Boolean> = agoraRepo.declineTheCall
    val callDuration = agoraRepo.callDuration
    val callEvent = agoraRepo.callEvent

    private val _uiState = MutableStateFlow(CallUIState())
    val uiState = _uiState.asStateFlow()


    init {


        // when the outgoing call gets declined, used to close the call
        val job = viewModelScope.launch {
            declineCall.collect { decline ->
                if (decline && !_uiState.value.callEnded) {

                    _uiState.update { it.copy(callEnded = true) }
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

                    _uiState.update { it.copy(callEnded = true) }
                }
            }
        }

        viewModelScope.launch {

            agoraRepo.isJoined.collect { isJoined ->
                if (isJoined) {

                    markCallServiceStarted()

                } else {
                    resetCallServiceFlag()
                }
            }
        }

    }


    // UI STATE

    fun markCallServiceStarted() {

        if (!_uiState.value.hasStartedService) _uiState.update { it.copy(hasStartedService = true) }

    }

    fun resetCallServiceFlag() {
        if (_uiState.value.hasStartedService) _uiState.update { it.copy(hasStartedService = false) }
    }

    fun setCallScreenData(data: CallMetadata?) {
        _uiState.update { it.copy(callMetadata = data) }

    }


    // CALL HANDLING

    fun startCallService(
        context: Context,
        callMetadata: CallMetadata
    ) {
        callUseCase.startCallUseCase(context, callMetadata)
    }

    fun stopCallService(context: Context) {

        callUseCase.endCallUseCase(context)
        _uiState.update { it.copy(callEnded = true) }


    }


    fun declineTheCall(decline: Boolean, callDocId: String) {

        callUseCase.declineCallUseCase(decline, callDocId)

    }

    // AUDIO CONTROLS

    fun muteOutgoingAudio() {

        _uiState.update { it.copy(isLocalAudioMuted = !uiState.value.isLocalAudioMuted) }
        audioUseCase.localAudioMuteUseCase(uiState.value.isLocalAudioMuted)
    }

    fun muteYourSpeaker() {


        _uiState.update { it.copy(isRemoteAudioDeafen = !uiState.value.isRemoteAudioDeafen) }

        audioUseCase.remoteAudioMuteUseCase(uiState.value.isRemoteAudioDeafen)
    }

    fun toggleSpeaker() {

        _uiState.update { it.copy(isSpeakerEnabled = !uiState.value.isSpeakerEnabled) }

        audioUseCase.toggleSpeakerUseCase(uiState.value.isSpeakerEnabled)
    }

    // VIDEO CONTROLS

    fun setUpLocalVideo(surfaceView: SurfaceView) {

        videoUseCase.setUpLocalVideoUseCase(surfaceView)

    }

    fun setUpRemoteVideo(surfaceView: SurfaceView, uid: Int) {

        videoUseCase.setUpRemoteVideoUseCase(surfaceView, uid)

    }


    fun switchCamera() {

        videoUseCase.switchCameraUseCase()
    }


    fun enableVideoPreview() {
        videoUseCase.enableVideoPreviewUseCase()
    }


    // ON DESTRUCTION OF THE VIEWMODEL

    override fun onCleared() {
        super.onCleared()
        Log.i("CHECK_VM", "viewmodel destroyed")


        // needed to reset the value when agoraCallService is not active
        // as the default value reset only works on destruction of the agoraCallService and
        // call service only activates if the user accepts the call or is a caller
       // agoraRepo.declineIncomingCall(false)
        agoraRepo.updateCallEvent(CallEvent.InActive)
    }


}