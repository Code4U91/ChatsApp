package com.example.chatapp.auth_feature.domain.repository

import android.app.Activity
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {

    suspend fun signInWithGoogle(activity: Activity): GoogleIdTokenCredential?

    suspend fun fireBaseAuthWithGoogle(
        idToken: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    suspend fun signInUsingEmailAndPwd(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    suspend fun signUpUsingEmailAndPwd(
        email: String,
        password: String,
        userName: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit

    )


    fun uploadUserDataInFireStore(userName: String, user: FirebaseUser, photoUrl: String)

    fun resetPassword(email: String): String

    fun removeFcmTokenFromUser(userId: String, token: String)

    fun updateUserEmail(
        newEmail: String,
        onFailure: (msg: String?) -> Unit,
        onSuccess: () -> Unit
    )

    suspend fun signOut()
}