package com.example.chatapp.auth_feature.data.repositoryIml

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.chatapp.R
import com.example.chatapp.auth_feature.domain.repository.AuthRepository
import com.example.chatapp.core.util.DEFAULT_PROFILE_PIC
import com.example.chatapp.core.util.USERS_COLLECTION
import com.example.chatapp.core.util.USERS_REF
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class AuthRepositoryIml(
    private val auth: FirebaseAuth,
    private val credentialManager: CredentialManager,
    private val firestoreDb: FirebaseFirestore,
    private val realTimeDb: FirebaseDatabase,
    private val firebaseMessaging: FirebaseMessaging,
    private val context: Context,
) : AuthRepository {

    override suspend fun signInWithGoogle(activity: Activity): GoogleIdTokenCredential? {

        var idToken: GoogleIdTokenCredential? = null

        // only use for automatic login credential pop up
        // when on sign in page it automatically slides up and shows available google ids from which user can sign in
//        val googleOption = GetGoogleIdOption.Builder()
//            .setFilterByAuthorizedAccounts(false)
//            .setServerClientId(context.getString(R.string.default_web_client_id))
//            .build()


        // Use this when you want to show the login option on click of the button by user
        val signInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(
                serverClientId = context.getString(R.string.default_web_client_id)
            )
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)  // put the one you want to use
            .build()


        return try {
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            if (credential.type == GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                idToken = GoogleIdTokenCredential.Companion.createFrom(credential.data)
            }

            return idToken

        } catch (e: GetCredentialException) {
            Log.e("GoogleSign", "Sign-In failed : ${e.message}")

            null
        }


    }


    override suspend fun fireBaseAuthWithGoogle(
        idToken: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (idToken == null) onFailure(Exception("Invalid token"))

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        try {
            auth.signInWithCredential(credential).await()

            // uploading new user's  data to firestore
            val user = auth.currentUser
            if (user != null) {
                uploadUserDataInFireStore(
                    user.displayName.toString(),
                    user,
                    user.photoUrl.toString()
                )

            }

            onSuccess()

        } catch (e: Exception) {
            onFailure(e)
        }
    }

    override suspend fun signInUsingEmailAndPwd(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            auth.signInWithEmailAndPassword(email, password).await()

            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }


    }

    override suspend fun signUpUsingEmailAndPwd(
        email: String,
        password: String,
        userName: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit

    ) {
        try {
            auth.createUserWithEmailAndPassword(email, password).await()

            val user = auth.currentUser
            if (user != null) {
                uploadUserDataInFireStore(
                    userName,
                    user,
                    DEFAULT_PROFILE_PIC
                )

            }

            onSuccess()
        } catch (e: Exception) {
            onFailure(e)

        }

    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }


    override fun uploadUserDataInFireStore(userName: String, user: FirebaseUser, photoUrl: String) {

        val userRef = firestoreDb.collection(USERS_COLLECTION).document(user.uid)

        userRef.get().addOnSuccessListener { document ->

            // check if the user database already exists or not
            if (!document.exists()) {

                firebaseMessaging.token.addOnSuccessListener { task ->

                    val newUser = mapOf(
                        "name" to userName,
                        "email" to user.email,
                        "photoUrl" to photoUrl,
                        "uid" to user.uid,
                        "about" to "....",
                        "fcmTokens" to listOf(task)
                    )
                    userRef.set(newUser)
                }
            }
        }


    }

    override fun resetPassword(email: String): String {
        return try {
            auth.sendPasswordResetEmail(email)
            "0"
        } catch (e: Exception) {
            e.message.toString()
        }

    }

    override fun removeFcmTokenFromUser(userId: String, token: String) {
        val userDoc = firestoreDb.collection(USERS_COLLECTION).document(userId)

        userDoc.update("fcmTokens", FieldValue.arrayRemove(token))
    }

    override fun updateUserEmail(
        newEmail: String,
        onFailure: (String?) -> Unit,
        onSuccess: () -> Unit
    ) {
        auth.currentUser?.verifyBeforeUpdateEmail(newEmail)?.addOnCompleteListener { task ->

            if (task.isSuccessful) {
                onSuccess
            } else {
                onFailure(task.exception?.message)
            }

        }
    }

    override suspend fun updateFcmTokenIfNeeded(savedTokens: List<String>) {
        val user = auth.currentUser ?: return

        val currentToken = firebaseMessaging.token.await()
        // save token locally using datastore
        // LocalFcmTokenManager.saveToken(context, currentToken)

        val userDoc = firestoreDb.collection(USERS_COLLECTION).document(user.uid)

        if (currentToken !in savedTokens) {
            //  adding only unique values automatically
            userDoc.update("fcmTokens", FieldValue.arrayUnion(currentToken))
                .addOnSuccessListener { Log.i("FCMCheck", "FCM Token updated: $currentToken") }
                .addOnFailureListener { Log.e("FCMError", "Failed to update token", it) }
        }
    }

    override suspend fun signOut() {

        val user = auth.currentUser ?: return

        val  currentFcmToken = runCatching {
            withTimeout(5000){
                firebaseMessaging.token.await()
            }
        }.getOrNull()

        runCatching {
            withTimeout(5000){
                val realTimeDbRef = realTimeDb.getReference(USERS_REF).child(user.uid)

                realTimeDbRef.setValue(
                    mapOf(
                        "onlineStatus" to false,
                        "lastSeen" to ServerValue.TIMESTAMP
                    )
                ).await()
            }
        }.onFailure {
            Log.e("SingOut", "failed to update status", it)
        }

         currentFcmToken?.let {
            removeFcmTokenFromUser(user.uid, it)
        }

        auth.signOut()


    }

}