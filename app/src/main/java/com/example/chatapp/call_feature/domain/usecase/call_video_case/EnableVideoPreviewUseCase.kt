package com.example.chatapp.call_feature.domain.usecase.call_video_case

import com.example.chatapp.call_feature.domain.repository.AgoraSetUpRepo

class EnableVideoPreviewUseCase(
    private val agoraSetUpRepo: AgoraSetUpRepo
) {
    operator fun invoke(){
        agoraSetUpRepo.enableVideo()
    }
}
