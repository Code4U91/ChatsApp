package com.example.chatapp.auth_feature.presentation.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.auth_feature.domain.usecase.auth_case.AuthUseCase
import com.example.chatapp.auth_feature.domain.usecase.online_state_case.OnlineStatusUseCase
import com.example.chatapp.core.model.MessageFcmMetadata
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val onlineStatusUseCase: OnlineStatusUseCase,
    private val authUseCase: AuthUseCase
) : ViewModel() {


    private var _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState = _authState.asStateFlow()

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
                onFailure = { exception ->
                    updateAuthState(AuthState.Error(exception.message.toString()))
                }
            )
        }
    }

    // Send password reset email to the email
    fun resetPasswordUsingEmail(email: String, onClick: (response: String) -> Unit) {

        val response = authUseCase.resetPasswordUseCase(email)
        onClick(response)
    }


    fun changeUserEmail(
        newEmail: String,
        onFailure: (msg: String?) -> Unit,
        onSuccess: () -> Unit
    ) {
        authUseCase.changeEmailUseCase(
            email = newEmail,
            onFailure = { msg -> onFailure(msg) },
            onSuccess = { onSuccess() }
        )
    }


    fun updateAuthState(newState: AuthState) {
        if (_authState.value != newState) {
            _authState.value = newState
        }
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