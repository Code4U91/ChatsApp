package com.example.chatapp.call.domain.usecase.session_case

import com.example.chatapp.call.domain.repository.CallSessionUploaderRepo

class UpdateCallStatus(
    private val callSessionUploaderRepo: CallSessionUploaderRepo
) {

    operator fun invoke(status: String, callId: String){

        callSessionUploaderRepo.updateCallStatus(status, callId)
    }
}