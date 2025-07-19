package com.example.chatapp.call_feature.domain.usecase.session_case

import com.example.chatapp.call_feature.domain.repository.CallSessionUploaderRepo

class UploadDataOnCallEnd(
    private val callSessionUploaderRepo: CallSessionUploaderRepo
) {

    operator fun invoke(
        status: String,
        callId: String
    ) {

        callSessionUploaderRepo.uploadOnCallEnd(status, callId)

    }
}