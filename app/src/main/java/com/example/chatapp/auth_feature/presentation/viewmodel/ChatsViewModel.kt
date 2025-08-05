package com.example.chatapp.auth_feature.presentation.viewmodel

import android.app.Activity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.auth_feature.domain.usecase.auth_case.AuthUseCase
import com.example.chatapp.auth_feature.domain.usecase.online_state_case.OnlineStatusUseCase
import com.example.chatapp.core.model.MessageFcmMetadata
import com.example.chatapp.core.util.USERS_COLLECTION
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val firestoreDb: FirebaseFirestore,
    private val onlineStatusUseCase: OnlineStatusUseCase,
    private val authUseCase: AuthUseCase
) : ViewModel() {


    // use shared flow
    private var _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState = _authState.asStateFlow()

    private var _loadingIndicator = MutableStateFlow(false)
    val loadingIndicator = _loadingIndicator.asStateFlow()

    private val _fcmMessageMetadata = MutableStateFlow<MessageFcmMetadata?>(null)
    val fcmMessageMetadata = _fcmMessageMetadata.asStateFlow()

    private val _isCallHistoryScreenActive = MutableStateFlow(false)
    val callHistoryScreenActive = _isCallHistoryScreenActive.asStateFlow()

    private val _moveToCallHistory = MutableStateFlow(false)
    val moveToCallHistory = _moveToCallHistory.asStateFlow()


    init {
        checkAuthStatus()

    }


    fun moveToCallHistory(move: Boolean) {
        _moveToCallHistory.value = move
    }

    fun setHistoryScreenActive(state: Boolean) {
        _isCallHistoryScreenActive.value = state
    }


    fun checkAuthStatus(user: FirebaseUser? = authUseCase.getCurrentUser()) {

        if (user != null) {

            updateAuthState(AuthState.Authenticated)

        } else {
            updateAuthState(AuthState.Unauthenticated)
        }
    }

    fun setFcmMessageMetaData(messageMetaData: MessageFcmMetadata?) {
        _fcmMessageMetadata.value = messageMetaData
    }


    // Sign in or Sign up using google credentials
    fun signInUsingGoogle(
        activity: Activity?
    ) {

        if (activity != null) {
            viewModelScope.launch {

                authUseCase.signInUseCase(
                    activity = activity,
                    withEmailAndPwd = true,
                    email = "",
                    password = "",
                    onSuccess = {
                        updateAuthState(AuthState.Authenticated)
                        setOnlineStatus()
                    },
                    onFailure = { exception ->
                        AuthState.Error(exception.message.toString())
                    },
                )
            }
        }

    }

    // Sign in using email and password
    fun signInUsingEmailAndPwd(email: String, password: String) {
        viewModelScope.launch {


            updateAuthState(AuthState.Loading)

            authUseCase.signInUseCase(
                activity = null,
                withEmailAndPwd = false,
                email = email,
                password = password,
                onSuccess = {
                    updateAuthState(AuthState.Authenticated)
                    setOnlineStatus()
                },
                onFailure = { exception ->
                    updateAuthState(AuthState.Error(exception.message.toString()))
                }
            )
        }
    }

    // Sign up using email and password
    fun signUpUsingEmailAndPwd(email: String, password: String, userName: String) {
        viewModelScope.launch {

            updateAuthState(AuthState.Loading)

            authUseCase.signUpUseCase(
                email = email,
                password = password,
                userName = userName,
                onSuccess = {
                    updateAuthState(AuthState.Authenticated)
                    setOnlineStatus()
                },
                onFailure = { exception -> updateAuthState(AuthState.Error(exception.message.toString()))
                }
            )
        }
    }

    // Send password reset email to the email
    fun resetPasswordUsingEmail(email: String, onClick: (response: String) -> Unit) {

        val response =  authUseCase.resetPasswordUseCase(email)
        onClick(response)
    }

    // updates basic user data in firestore database and firebase auth
    // migrate to use case and put it in profile feature
    fun updateUserData(
        newData: Map<String, Any?>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        // move to use case
        val user = authUseCase.getCurrentUser()
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

    // uploads the user data on both firestore database and firebase auth
    // move to use case
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

    fun updateUserEmail(
        newEmail: String,
        onFailure: (msg: String?) -> Unit,
        onSuccess: () -> Unit
    ) {
        val user =  authUseCase.getCurrentUser()
        val userId = user?.uid

        if (user != null && userId != null) {
            user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                    // signOut()
                } else {
                    onFailure(task.exception?.message)
                }
            }
        }
    }


    fun checkAndUpdateEmailOnFireStore(
        currentEmailInDb: String,
    ) {
        val user =  authUseCase.getCurrentUser() ?: return

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


    fun updateAuthState(newState: AuthState) {
        if (_authState.value != newState) {
            _authState.value = newState
        }
    }

    fun updateLoadingIndicator(state: Boolean) {
        _loadingIndicator.value = state
    }


    private fun setOnlineStatus(status: Boolean = true) {
        onlineStatusUseCase.setOnlineStatus(status)
    }

}

sealed class AuthState {
    data object Authenticated : AuthState() // updated object to data object
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Error(val message: String?) : AuthState()
}