package com.code4u.chatsapp.call_feature.domain.usecase.call_video_case

import android.view.SurfaceView
import com.code4u.chatsapp.call_feature.domain.repository.AgoraSetUpRepo

class SetUpRemoteVideoUseCase(
    private val agoraSetUpRepo: AgoraSetUpRepo
) {

    operator fun invoke(surfaceView: SurfaceView, uid : Int){

        agoraSetUpRepo.setupRemoteVideo(surfaceView, uid)
    }

}