package com.example.chatapp.call.domain.usecase.call_case

import com.example.chatapp.call.domain.repository.AgoraSetUpRepo
import com.example.chatapp.call.domain.repository.CallSessionUploaderRepo
import com.example.chatapp.call.presentation.call_screen.state.CallEvent

class DeclineCallUseCase(
    private val agoraSetUpRepo: AgoraSetUpRepo,
    private val callSessionUploaderRepo: CallSessionUploaderRepo
) {
    operator fun invoke(decline: Boolean, callDocId: String){

        callSessionUploaderRepo.updateCallStatus("declined", callDocId)
        agoraSetUpRepo.updateCallEvent(CallEvent.Ended)
        //agoraSetUpRepo.declineIncomingCall(decline) testing
    }

}