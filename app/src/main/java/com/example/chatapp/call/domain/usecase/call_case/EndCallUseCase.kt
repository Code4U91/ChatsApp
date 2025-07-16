package com.example.chatapp.call.domain.usecase.call_case

import android.content.Context
import android.content.Intent
import com.example.chatapp.call.domain.repository.AgoraSetUpRepo
import com.example.chatapp.call.presentation.call_screen.state.CallEvent
import com.example.chatapp.service.AgoraCallService

class EndCallUseCase(
    private val agoraSetUpRepo: AgoraSetUpRepo
) {
    operator fun invoke(context: Context) {
        context.stopService(Intent(context, AgoraCallService::class.java))
        //agoraSetUpRepo.updateCallEvent(CallEvent.Ended)
    }
}