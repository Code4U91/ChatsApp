package com.code4u.chatsapp.call_feature.domain.usecase.call_video_case

import com.code4u.chatsapp.call_feature.domain.repository.AgoraSetUpRepo

class SwitchCameraUseCase(
    private val agoraSetUpRepo: AgoraSetUpRepo
) {
    operator fun invoke(){
        agoraSetUpRepo.switchCamera()
    }
}