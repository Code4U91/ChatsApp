package com.example.chatapp.call.domain.usecase.audio_case

import com.example.chatapp.call.domain.repository.AgoraSetUpRepo

class LocalAudioMuteUseCase(
    private val agoraSetUpRepo: AgoraSetUpRepo
) {

    operator fun invoke(mute : Boolean){

        agoraSetUpRepo.muteLocalAudio(mute)
    }

}