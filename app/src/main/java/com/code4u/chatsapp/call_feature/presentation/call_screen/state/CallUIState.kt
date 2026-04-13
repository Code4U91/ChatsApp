package com.code4u.chatsapp.call_feature.presentation.call_screen.state

import com.code4u.chatsapp.core.model.CallMetadata

data class CallUIState(
    val isLocalAudioMuted : Boolean = false,
    val isSpeakerEnabled : Boolean = false,
    val isRemoteAudioDeafen: Boolean = false,
    val callEnded: Boolean = false,
    val callMetadata : CallMetadata? = null,
    val hasStartedService: Boolean = false
)