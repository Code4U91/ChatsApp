package com.example.chatapp.call_feature.domain.usecase.call_video_case

import com.example.chatapp.call_feature.domain.repository.AgoraSetUpRepo

class SwitchCameraUseCase(
    private val agoraSetUpRepo: AgoraSetUpRepo
) {
    operator fun invoke(){
        agoraSetUpRepo.switchCamera()
    }
}