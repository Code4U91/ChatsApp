package com.example.chatapp.call.domain.usecase.call_video_case

import android.view.SurfaceView
import com.example.chatapp.call.domain.repository.AgoraSetUpRepo

class SetUpLocalVideoUseCase(
    private val agoraSetUpRepo: AgoraSetUpRepo
) {
    operator fun invoke(surfaceView: SurfaceView){

        agoraSetUpRepo.setupLocalVideo(surfaceView)
    }

}