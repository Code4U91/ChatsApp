package com.example.chatapp.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.chatapp.DEFAULT_PROFILE_PIC
import com.example.chatapp.R
import com.example.chatapp.USERS_COLLECTION
import com.example.chatapp.USERS_REF
import com.example.chatapp.localData.dataStore.LocalFcmTokenManager
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Contains all the functions needed to authenticate user
// also uploads initial user data to firestore on first signup

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val credentialManager: CredentialManager,
    private val firestoreDb: FirebaseFirestore,
    private val realTimeDb: FirebaseDatabase,
    private val firebaseMessaging: FirebaseMessaging,
    @ApplicationContext private val context: Context,
) {

    suspend fun signInWithGoogle(activity: Activity): GoogleIdTokenCredential? {

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

            if (credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                idToken = GoogleIdTokenCredential.createFrom(credential.data)
            }

            return idToken

        } catch (e: GetCredentialException) {
            Log.e("GoogleSign", "Sign-In failed : ${e.message}")

            null
        }


    }


    suspend fun fireBaseAuthWithGoogle(
        idToken: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

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

    suspend fun signInUsingEmailAndPwd(
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

    suspend fun signUpUsingEmailAndPwd(
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


    private fun uploadUserDataInFireStore(userName: String, user: FirebaseUser, photoUrl: String) {

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

    fun resetPassword(email: String): String {
        return try {
            auth.sendPasswordResetEmail(email)
            "0"
        } catch (e: Exception) {
            e.message.toString()
        }

    }

    private fun removeFcmTokenFromUser(userId: String, token: String) {
        val userDoc = firestoreDb.collection(USERS_COLLECTION).document(userId)

        userDoc.update("fcmTokens", FieldValue.arrayRemove(token))
    }

    suspend fun signOut() {

        val user = auth.currentUser
        if (user != null) {

            val userId = user.uid
            val token = LocalFcmTokenManager.getToken(context)
            val realTimeDbRef = realTimeDb.getReference(USERS_REF).child(user.uid)

            realTimeDbRef.setValue(
                mapOf(
                    "onlineStatus" to false,
                    "lastSeen" to ServerValue.TIMESTAMP
                )
            ).await()

            token?.let {
                removeFcmTokenFromUser(userId, it)
                LocalFcmTokenManager.clearToken(context)
            }
            auth.signOut()
        }

    }

}


