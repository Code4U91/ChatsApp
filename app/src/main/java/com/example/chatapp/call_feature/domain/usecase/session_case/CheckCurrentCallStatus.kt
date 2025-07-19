package com.example.chatapp.call_feature.domain.usecase.session_case

import com.example.chatapp.call_feature.domain.repository.CallSessionUploaderRepo
import com.google.firebase.firestore.ListenerRegistration

class CheckCurrentCallStatus(
    private val callSessionUploaderRepo: CallSessionUploaderRepo
) {
    operator fun invoke(callId: String, onCallDeclined: () -> Unit) : ListenerRegistration {

        return callSessionUploaderRepo.checkAndUpdateCurrentCall(
            callId = callId,
            onCallDeclined = { onCallDeclined() })
    }
}