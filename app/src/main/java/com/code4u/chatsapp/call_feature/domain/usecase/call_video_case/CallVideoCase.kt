package com.code4u.chatsapp.call_feature.domain.usecase.call_video_case

data class CallVideoCase(
    val enableVideoPreviewUseCase: EnableVideoPreviewUseCase,
    val setUpRemoteVideoUseCase: SetUpRemoteVideoUseCase,
    val setUpLocalVideoUseCase: SetUpLocalVideoUseCase,
    val switchCameraUseCase: SwitchCameraUseCase
)