package com.example.chatapp.call.domain.usecase.audio_case

data class CallAudioCase (
    val localAudioMuteUseCase: LocalAudioMuteUseCase,
    val remoteAudioMuteUseCase: RemoteAudioMuteUseCase,
    val toggleSpeakerUseCase: ToggleSpeakerUseCase
)