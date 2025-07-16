package com.example.chatapp.call.domain.usecase.call_video_case

import android.view.SurfaceView
import com.example.chatapp.call.domain.repository.AgoraSetUpRepo

class SetUpRemoteVideoUseCase(
    private val agoraSetUpRepo: AgoraSetUpRepo
) {

    operator fun invoke(surfaceView: SurfaceView, uid : Int){

        agoraSetUpRepo.setupRemoteVideo(surfaceView, uid)
    }

}