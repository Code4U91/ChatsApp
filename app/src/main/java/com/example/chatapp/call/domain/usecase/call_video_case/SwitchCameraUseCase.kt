package com.example.chatapp.call.domain.usecase.call_video_case

import com.example.chatapp.call.domain.repository.AgoraSetUpRepo

class SwitchCameraUseCase(
    private val agoraSetUpRepo: AgoraSetUpRepo
) {
    operator fun invoke(){
        agoraSetUpRepo.switchCamera()
    }
}