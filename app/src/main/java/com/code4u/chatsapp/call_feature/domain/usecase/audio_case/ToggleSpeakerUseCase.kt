package com.code4u.chatsapp.call_feature.domain.usecase.audio_case

import com.code4u.chatsapp.call_feature.domain.repository.AgoraSetUpRepo

class ToggleSpeakerUseCase(
    private val agoraSetUpRepo: AgoraSetUpRepo
) {
    operator fun invoke(isSpeakerPhoneEnabled: Boolean){

        agoraSetUpRepo.toggleSpeakerphone(isSpeakerPhoneEnabled)
    }
}