package com.example.chatapp.call_feature.domain.usecase.audio_case

import com.example.chatapp.call_feature.domain.repository.AgoraSetUpRepo

class LocalAudioMuteUseCase(
    private val agoraSetUpRepo: AgoraSetUpRepo
) {

    operator fun invoke(mute : Boolean){

        agoraSetUpRepo.muteLocalAudio(mute)
    }

}