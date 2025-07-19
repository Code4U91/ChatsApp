package com.example.chatapp.call_feature.presentation.call_screen.state

import com.example.chatapp.core.model.CallMetadata

data class CallUIState(
    val isLocalAudioMuted : Boolean = false,
    val isSpeakerEnabled : Boolean = false,
    val isRemoteAudioDeafen: Boolean = false,
    val callEnded: Boolean = false,
    val callMetadata : CallMetadata? = null,
    val hasStartedService: Boolean = false
)