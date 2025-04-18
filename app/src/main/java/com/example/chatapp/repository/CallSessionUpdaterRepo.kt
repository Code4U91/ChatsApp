package com.example.chatapp.repository

import com.example.chatapp.CALL_HISTORY
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject
import javax.inject.Singleton

// class used for creating a session/call document which maintains the call data and later
// is used as a record in a call history
@Singleton
class CallSessionUpdaterRepo @Inject constructor(
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

            val stateValueInDb = doc.getString("status")

            val list = listOf("missed", "ended")

            if (doc.exists() && stateValueInDb != null) {

                if (stateValueInDb !in list)
                {
                    val newStatus = mapOf(
                        "status" to status,
                        "callEndTime" to Timestamp.now()
                    )

                    callDocRef.update(newStatus)
                }

            }


        }


    }

    fun uploadOnCallEnd(
        status: String,
        callId: String
    ) {


        val callDocRef = firestoreDb.collection(CALL_HISTORY).document(callId)

        callDocRef.get().addOnSuccessListener {


            val callEndTimeData = mapOf(
                "callEndTime" to Timestamp.now(),
                "status" to status
            )
            callDocRef.update(callEndTimeData)
        }
    }


    fun checkAndUpdateCurrentCall(
        callId: String,
        onCallDeclined: () -> Unit
    ): ListenerRegistration {

        val currentCallListener = firestoreDb.collection(CALL_HISTORY).document(callId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {

                    val getCallStatus = snapshot.getString("status")

                    getCallStatus?.let { currentCallStatus ->

                        if (currentCallStatus == "declined" || currentCallStatus == "missed") {
                            onCallDeclined()
                        }

                    }

                }


            }


        return currentCallListener

    }


}



