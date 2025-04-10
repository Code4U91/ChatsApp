package com.example.chatapp.viewmodel

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.CallMetadata
import com.example.chatapp.USERS_COLLECTION
import com.example.chatapp.repository.AgoraSetUpRepo
import com.example.chatapp.repository.AuthRepository
import com.example.chatapp.repository.ChatManager
import com.example.chatapp.repository.MessageServiceRepository
import com.example.chatapp.repository.OnlineStatusRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val auth: FirebaseAuth,
    private val firestoreDb: FirebaseFirestore,
    private val messageServiceRepository: MessageServiceRepository,
    private val chatManager: ChatManager,
    private val onlineStatusRepo: OnlineStatusRepo,
    private val agoraRepo: AgoraSetUpRepo
) : ViewModel() {


    private var _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    private var _loadingIndicator = MutableStateFlow(false)
    val loadingIndicator: StateFlow<Boolean> = _loadingIndicator

    private val _deepLinkData = MutableStateFlow<CallMetadata?>(null)
    val deepLinkData = _deepLinkData.asStateFlow()

    private val _isCallScreenActive = mutableStateOf(false)
    val isCallScreenActive: State<Boolean> get() = _isCallScreenActive

    private val _hasStartedCallService = mutableStateOf(false)
    val hasStartedCallService: State<Boolean> get() = _hasStartedCallService

    fun markCallServiceStarted() {

        if (!_hasStartedCallService.value) _hasStartedCallService.value = true

    }

    fun resetCallServiceFlag() {
        if (_hasStartedCallService.value) _hasStartedCallService.value = false
    }


    fun setCallScreenActive(active: Boolean) {
        _isCallScreenActive.value = active
    }



    init {
        checkAuthStatus()

        // collecting it so that even if the app restarts, we always will know if the call is active or not
        // if the app was not to be killed then we could just have relied on global viewmodel to save the state
        // agora is injected to service class so it should survive the app close
        viewModelScope.launch {

            agoraRepo.isJoined.collect{isJoined ->

                Log.i("CHATS_VM_IS_JOINED", isJoined.toString())
                if(isJoined)
                {
                    markCallServiceStarted()
                } else {
                    resetCallServiceFlag()
                }
            }
        }
    }


    fun checkAuthStatus(user: FirebaseUser? = auth.currentUser) {

        if (user != null) {

            updateAuthState(AuthState.Authenticated)

        } else {
            updateAuthState(AuthState.Unauthenticated)
        }
    }

    fun setDeepLinkData(metadata: CallMetadata?)
    {
        _deepLinkData.value = metadata

    }


    // Sign in or Sign up using google credentials
    fun signInUsingGoogle(
        activity: Activity?
    ) {

        if (activity != null) {
            viewModelScope.launch {

                val idToken = authRepository.signInWithGoogle(activity)

                if (idToken != null) {

                    updateAuthState(AuthState.Loading)

                    authRepository.fireBaseAuthWithGoogle(
                        idToken.idToken,
                        onSuccess = {

                            updateAuthState(AuthState.Authenticated)
                            setOnlineStatus()

                        }, onFailure = { exception ->
                            AuthState.Error(exception.message.toString())
                        })


                }
            }
        }

    }

    // Sign in using email and password
    fun signInUsingEmailAndPwd(email: String, password: String) {
        viewModelScope.launch {


            updateAuthState(AuthState.Loading)
            authRepository.signInUsingEmailAndPwd(
                email,
                password,
                onSuccess = {
                    updateAuthState(AuthState.Authenticated)
                    setOnlineStatus()
                },
                onFailure = { exception -> updateAuthState(AuthState.Error(exception.message.toString())) }
            )
        }
    }

    // Sign up using email and password
    fun signUpUsingEmailAndPwd(email: String, password: String, userName: String) {
        viewModelScope.launch {

            updateAuthState(AuthState.Loading)
            authRepository.signUpUsingEmailAndPwd(
                email,
                password,
                userName,
                onSuccess = {
                    updateAuthState(AuthState.Authenticated)
                    setOnlineStatus()
                },
                onFailure = { exception -> updateAuthState(AuthState.Error(exception.message.toString())) }
            )
        }
    }

    // Send password reset email to the email
    fun resetPasswordUsingEmail(email: String, onClick: (response: String) -> Unit) {
        val response = authRepository.resetPassword(email)
        onClick(response)
    }

    // updates basic user data in firestore database and firebase auth
    fun updateUserData(
        newData: Map<String, Any?>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val user = auth.currentUser
        val name = newData["name"] as? String
        val photoUrl = newData["photoUrl"] as? String
        val about = newData["about"] as? String

        if (user != null) {

            if (name != null) {
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                updateProfile(
                    user,
                    profileUpdates,
                    newData,
                    onSuccess = { onSuccess() },
                    onFailure = { exception -> onFailure(exception) }
                )
            }

            if (photoUrl != null) {
                val profileUpdates = userProfileChangeRequest {
                    displayName = photoUrl
                }

                updateProfile(
                    user,
                    profileUpdates,
                    newData,
                    onSuccess = { onSuccess() },
                    onFailure = { exception -> onFailure(exception) }
                )
            }

            if (about != null) {
                uploadInDb(
                    mapOf("about" to about),
                    user = user,
                    onSuccess = { onSuccess() },
                    onFailure = { exception -> onFailure(exception) }
                )
            }
        }

    }

    // uploads the user data on both firestore database and firebase auth
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
        val user = auth.currentUser
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
        val user = auth.currentUser ?: return

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


    fun signOut() {

        viewModelScope.launch {

            chatManager.clearAllGlobalListeners()
            messageServiceRepository.clearMessageListeners()
            authRepository.signOut()

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
        onlineStatusRepo.setOnlineStatusWithDisconnect(status)
    }

}

sealed class AuthState {
    data object Authenticated : AuthState() // updated object to data object
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Error(val message: String?) : AuthState()
}