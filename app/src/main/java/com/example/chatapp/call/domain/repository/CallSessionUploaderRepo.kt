package com.example.chatapp.call.domain.repository

import com.google.firebase.firestore.ListenerRegistration

interface CallSessionUploaderRepo {

    fun uploadCallData(
        callReceiverId: String,
        callType: String,
        channelId: String,
        callStatus: String,
        callerName: String,
        receiverName: String
    ): String

    fun updateCallStatus(status: String, callId: String)

    fun uploadOnCallEnd(
        status: String,
        callId: String
    )


    fun checkAndUpdateCurrentCall(
        callId: String,
        onCallDeclined: () -> Unit
    ): ListenerRegistration
}