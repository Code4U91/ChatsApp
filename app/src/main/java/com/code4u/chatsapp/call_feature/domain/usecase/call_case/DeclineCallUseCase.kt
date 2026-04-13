package com.code4u.chatsapp.call_feature.domain.usecase.call_case

import com.code4u.chatsapp.call_feature.domain.repository.AgoraSetUpRepo
import com.code4u.chatsapp.call_feature.domain.repository.CallSessionUploaderRepo
import com.code4u.chatsapp.call_feature.presentation.call_screen.state.CallEvent

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