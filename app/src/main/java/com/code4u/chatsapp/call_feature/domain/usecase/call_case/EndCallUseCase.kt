package com.code4u.chatsapp.call_feature.domain.usecase.call_case

import android.content.Context
import android.content.Intent
import com.code4u.chatsapp.service.AgoraCallService

class EndCallUseCase {
    operator fun invoke(context: Context) {
        context.stopService(Intent(context, AgoraCallService::class.java))
    }
}