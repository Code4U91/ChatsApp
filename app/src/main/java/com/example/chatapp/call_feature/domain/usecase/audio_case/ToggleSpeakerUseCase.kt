package com.example.chatapp.call_feature.domain.usecase.audio_case

import com.example.chatapp.call_feature.domain.repository.AgoraSetUpRepo

class ToggleSpeakerUseCase(
    private val agoraSetUpRepo: AgoraSetUpRepo
) {
    operator fun invoke(isSpeakerPhoneEnabled: Boolean){

        agoraSetUpRepo.toggleSpeakerphone(isSpeakerPhoneEnabled)
    }
}