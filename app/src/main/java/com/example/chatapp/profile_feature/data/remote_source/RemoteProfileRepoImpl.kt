package com.example.chatapp.profile_feature.data.remote_source

import android.util.Log
import androidx.core.net.toUri
import com.example.chatapp.core.util.USERS_COLLECTION
import com.example.chatapp.profile_feature.domain.repository.RemoteProfileRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RemoteProfileRepoImpl(
    private val firestoreDb: FirebaseFirestore,
    private val auth: FirebaseAuth
) : RemoteProfileRepo {

    private var listener: ListenerRegistration? = null

    // function to fetch current user data

    override fun fetchUserData(): Flow<UserData?> = callbackFlow {

        val user = auth.currentUser

        if(user == null){
            close()
            return@callbackFlow
        }

        val userRef = firestoreDb.collection(USERS_COLLECTION)
            .document(user.uid)

        listener = userRef.addSnapshotListener { snapshot, error ->
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
            listener?.remove()
        }
    }

    override suspend fun oneTimeUserDataFetch(): UserData? {

        return auth.currentUser?.uid?.let {

            try {
                val snapshot = firestoreDb.collection(USERS_COLLECTION)
                    .document(it)
                    .get()
                    .await()

                if (snapshot.exists()) {
                    snapshot.toObject(UserData::class.java)
                } else {
                    null
                }

            } catch (e: Exception) {

                Log.e("FETCH_USER_DATA", "Error fetching user data for id=$it", e)
                null
            }
        }


    }

    override fun updateUserData(
        newData: Map<String, Any?>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // move to use case
        val user =  auth.currentUser
        val name = newData["name"] as? String
        val photoUrl = newData["photoUrl"] as? String
        val about = newData["about"] as? String

        user?.let {

            if (name != null) {
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                updateProfile(
                    it,
                    profileUpdates,
                    newData,
                    onSuccess = { onSuccess() },
                    onFailure = { exception -> onFailure(exception) }
                )
            }

            if (photoUrl != null) {

                val profileUpdates = userProfileChangeRequest {
                    photoUri = photoUrl.toUri()
                }

                updateProfile(
                    it,
                    profileUpdates,
                    newData,
                    onSuccess = { onSuccess() },
                    onFailure = { exception -> onFailure(exception) }
                )
            }

            if (about != null) {
                uploadInDb(
                    mapOf("about" to about),
                    user = it,
                    onSuccess = { onSuccess() },
                    onFailure = { exception -> onFailure(exception) }
                )
            }
        }
    }

    private fun updateProfile(
        user: FirebaseUser,
        profileUpdates: UserProfileChangeRequest,
        newData: Map<String, Any?>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                uploadInDb(
                    newData,
                    user,
                    onSuccess = { onSuccess() },
                    onFailure = { exception -> onFailure(exception) }
                )
            } else {
                onFailure(Exception(task.exception))
            }
        }

    }

    override fun checkAndUpdateEmailOnFireStore(
        currentEmailInDb: String,
    ) {
        val user =  auth.currentUser ?: return

        user.let { currentUser ->

            val currentEmail = user.email

            if (currentEmail != currentEmailInDb) {
                // update in db
                val userData = mapOf("email" to currentEmail)

                uploadInDb(
                    userData,
                    currentUser,
                    onSuccess = {},
                    onFailure = {}
                )
            }

        }

    }

    private fun uploadInDb(
        newData: Map<String, Any?>,
        user: FirebaseUser,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestoreDb.collection(USERS_COLLECTION).document(user.uid)
            .update(newData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

}