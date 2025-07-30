package com.example.chatapp.call_feature.data.remote_source.repositoryImpl

import android.util.Log
import com.example.chatapp.call_feature.data.remote_source.model.CallData
import com.example.chatapp.call_feature.domain.repository.RemoteCallRepo
import com.example.chatapp.core.CALL_HISTORY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class RemoteCallRepoIml(
    private val auth: FirebaseAuth,
    private val firestoreDb: FirebaseFirestore
) : RemoteCallRepo {

    override fun fetchCallHistory(): Flow<List<CallData>> = callbackFlow {

        auth.currentUser?.let { user ->
            val currentUserId = user.uid

            val listener = firestoreDb.collection(CALL_HISTORY)
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener { querySnapshot, error ->

                    if (error != null) {
                        Log.e("Firestore", "Error fetching messages", error)
                        return@addSnapshotListener
                    }
                    val callList = querySnapshot?.documents?.mapNotNull { doc ->

                        // multiple .where is causing issue so filtering the rest of data here
                        val status = doc.getString("status") ?: return@mapNotNull null
                        if (status == "ringing" || status == "ongoing") return@mapNotNull null

                        @Suppress("UNCHECKED_CAST")
                        val participants =
                            doc.get("participants") as? List<String> ?: return@mapNotNull null

                        val otherUserId =
                            participants.firstOrNull { it != currentUserId } // pulling other participant

                        @Suppress("UNCHECKED_CAST")
                        val participantsName = doc.get("participantsName") as? Map<String, String>

                        val otherUserName = participantsName?.get(otherUserId)

                        doc.toObject(CallData::class.java)?.copy(
                            callId = doc.id,
                            otherUserName = otherUserName.orEmpty(),
                            otherUserId = otherUserId.orEmpty() // other participant
                        )

                    } ?: emptyList()

                    trySend(callList).isSuccess


                }

            awaitClose { listener.remove() }
        }


    }
}