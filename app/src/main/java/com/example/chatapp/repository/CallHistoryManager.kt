package com.example.chatapp.repository

import com.example.chatapp.CALL_HISTORY
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class CallHistoryManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestoreDb: FirebaseFirestore
) {


    fun uploadCallData(
        callReceiverId: String,
        callType: String,
        channelId: String,
        callStatus: String,
        callerName: String,
        receiverName: String
    ): String {

        val userId = auth.currentUser?.uid


        val callDocRef = firestoreDb.collection(CALL_HISTORY).document()

        val mapIdWithName = mapOf(
            userId to callerName,
            callReceiverId to receiverName
        )

        val callData = mapOf(
            "callerId" to userId,
            "callReceiverId" to callReceiverId,
            "callType" to callType,
            "channelId" to channelId,
            "status" to callStatus,
            "callStartTime" to Timestamp.now(),
            "participants" to listOf(userId, callReceiverId),
            "participantsName" to mapIdWithName
        )

        callDocRef.set(callData)

        return callDocRef.id

    }

    fun updateCallStatus(status: String, callId: String) {

        val callDocRef = firestoreDb.collection(CALL_HISTORY).document(callId)

        callDocRef.get().addOnSuccessListener { doc ->

            if (doc.exists()) {
                val newStatus = mapOf(
                    "status" to status,
                )

                callDocRef.update(newStatus)
            }


        }


    }

    fun uploadOnCallEnd(
        status: String,
        callId: String
    ) {


        val callDocRef = firestoreDb.collection(CALL_HISTORY).document(callId)
        val callEndTimeData = mapOf(
            "callEndTime" to Timestamp.now(),
            "status" to status
        )
        callDocRef.update(callEndTimeData)


    }
}

