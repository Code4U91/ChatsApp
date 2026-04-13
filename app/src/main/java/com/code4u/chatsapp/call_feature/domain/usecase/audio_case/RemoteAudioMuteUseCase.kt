package com.code4u.chatsapp.call_feature.domain.usecase.audio_case

import com.code4u.chatsapp.call_feature.domain.repository.AgoraSetUpRepo

class RemoteAudioMuteUseCase (
    private val agoraSetUpRepo: AgoraSetUpRepo
){
    operator fun invoke(mute : Boolean){
        agoraSetUpRepo.muteRemoteAudio(mute)
    }
}