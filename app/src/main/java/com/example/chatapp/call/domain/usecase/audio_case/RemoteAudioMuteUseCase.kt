package com.example.chatapp.call.domain.usecase.audio_case

import com.example.chatapp.call.domain.repository.AgoraSetUpRepo

class RemoteAudioMuteUseCase (
    private val agoraSetUpRepo: AgoraSetUpRepo
){
    operator fun invoke(mute : Boolean){
        agoraSetUpRepo.muteRemoteAudio(mute)
    }
}