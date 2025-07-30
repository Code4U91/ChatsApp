package com.example.chatapp.profile_feature.data.remote_source

import com.example.chatapp.core.USERS_COLLECTION
import com.example.chatapp.profile_feature.domain.repository.RemoteProfileRepo
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class RemoteProfileRepoImpl(
    private val firestoreDb: FirebaseFirestore
) : RemoteProfileRepo {


    private var listenerRegistration : ListenerRegistration? = null

    // function to fetch current user data
    override fun fetchUserData(user: FirebaseUser) : Flow<UserData?> = callbackFlow{

        val userRef = firestoreDb.collection(USERS_COLLECTION).document(user.uid)

        listenerRegistration  = userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {

                val fetchedUserData = snapshot.toObject(UserData::class.java)

                @Suppress("UNCHECKED_CAST")
                val fcmTokens = snapshot.get("fcmTokens") as? List<String> ?: emptyList()

                val userData = fetchedUserData?.copy(
                    fcmTokens = fcmTokens
                )
                trySend(userData)
            }
        }

        awaitClose {
            listenerRegistration?.remove()
            listenerRegistration = null
        }


    }

    override fun clearUserDataListener() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}