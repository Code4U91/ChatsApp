package com.code4u.chatsapp.call_feature.domain.usecase.call_case

import android.content.Context
import android.content.Intent
import com.code4u.chatsapp.call_feature.domain.repository.AgoraSetUpRepo
import com.code4u.chatsapp.service.AgoraCallService

class EndCallUseCase(
    private val agoraSetUpRepo: AgoraSetUpRepo
) {
    operator fun invoke(context: Context) {
        context.stopService(Intent(context, AgoraCallService::class.java))
        //agoraSetUpRepo.updateCallEvent(CallEvent.Ended)
    }
}