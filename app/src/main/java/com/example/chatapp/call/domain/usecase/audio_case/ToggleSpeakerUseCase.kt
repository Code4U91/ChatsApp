package com.example.chatapp.call.domain.usecase.audio_case

import com.example.chatapp.call.domain.repository.AgoraSetUpRepo

class ToggleSpeakerUseCase(
    private val agoraSetUpRepo: AgoraSetUpRepo
) {
    operator fun invoke(isSpeakerPhoneEnabled: Boolean){

        agoraSetUpRepo.toggleSpeakerphone(isSpeakerPhoneEnabled)
    }
}