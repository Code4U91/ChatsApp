package com.example.chatapp.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.ChatItemData
import com.example.chatapp.FriendData
import com.example.chatapp.Message
import com.example.chatapp.USERS_COLLECTION
import com.example.chatapp.UserData
import com.example.chatapp.repository.AuthRepository
import com.example.chatapp.repository.ChatManager
import com.example.chatapp.repository.MessageServiceRepository
import com.example.chatapp.repository.OnlineStatusRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val auth: FirebaseAuth,
    private val firestoreDb: FirebaseFirestore,
    private val messageServiceRepository: MessageServiceRepository,
    private val chatManager: ChatManager,
    private val onlineStatusRepo: OnlineStatusRepo
) : ViewModel() {


    private var _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    private var _loadingIndicator = MutableStateFlow(false)
    val loadingIndicator: StateFlow<Boolean> = _loadingIndicator

    private val _totalFriend = MutableStateFlow(0)
    val totalFriend: StateFlow<Int> = _totalFriend

    private val _friendData = MutableStateFlow<FriendData?>(null)
    val friendData: StateFlow<FriendData?> = _friendData

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData = _userData.asStateFlow()

    private val _currentOpenChatId = MutableStateFlow<String?>(null)
    val currentOpenChatId = _currentOpenChatId.asStateFlow()

    private val _activeChatList = MutableStateFlow<List<ChatItemData>>(emptyList())
    val activeChatList = _activeChatList.asStateFlow()


    init {
        checkAuthStatus()

        viewModelScope.launch {
            authState.collect{ state ->

                if (state is AuthState.Authenticated)
                {
                    startGlobalListener()
                    fetchUserData()
                    setOnlineStatus() // marks as true
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

    fun updateFriendData(friendData: FriendData) {
        _friendData.value = friendData

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
                            setOnlineStatus(true)

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
                    setOnlineStatus(true)
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
                    setOnlineStatus(true)
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
    fun updateProfile(
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

    fun uploadInDb(
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
        onAuthUpdateFail: (msg: String?) -> Unit,
        onSuccess: () -> Unit
    ) {
        val user = auth.currentUser
        val userId = user?.uid

        if (user != null && userId != null) {
            user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                    signOut()
                } else {
                    onAuthUpdateFail(task.exception?.message)
                }
            }
        }
    }

    fun checkAndUpdateEmail(user: FirebaseUser) {

        user.reload().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val updatedUser = auth.currentUser  // Refresh user instance
                if (updatedUser != null && updatedUser.isEmailVerified) {

                    val verifiedEmail = updatedUser.email

                    if (!verifiedEmail.isNullOrEmpty()) {

                        val userData = mapOf("email" to verifiedEmail)

                        updateUserData(
                            userData,
                            onSuccess = { },
                            onFailure = { }
                        )
                    }
                }
            }
        }
    }


    fun signOut() {
        viewModelScope.launch {

            authRepository.signOut()
            {
                checkAuthStatus()
                chatManager.clearAllGlobalListeners()
                messageServiceRepository.clearMessageListeners()

            }

        }

    }

    private fun fetchUserData() {
        val user = auth.currentUser
        if (user != null) {
             messageServiceRepository.fetchUserData(user)
            { updatedUserData ->

                _userData.value = updatedUserData
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

    fun addNewFriend(
        friendUserId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        messageServiceRepository.addFriend(
            friendUserId,
            onSuccess = { onSuccess() },
            onFailure = { e -> onFailure(e) }
        )
    }

    fun fetchFriendList(onFriendUpdated: (List<DocumentSnapshot>) -> Unit) {

        messageServiceRepository.fetchFriendList { friendDocument, updatedTotalFriend ->
            onFriendUpdated(friendDocument)
            _totalFriend.value = updatedTotalFriend
        }
    }

    fun fetchFriendData(friendUserId: String, updatedFriendData: (FriendData?) -> Unit): ListenerRegistration {

      return messageServiceRepository.fetchFriendData(friendUserId)
        {
            updatedFriendData(it) // used where multiple new friend data is required at once

            if (friendData.value?.uid == friendUserId) {
                _friendData.value = it  // this one has same friend data at all place
            }

        }

    }

    fun deleteFriend(friendId: String) {
        messageServiceRepository.deleteFriend(friendId)
    }

    fun sendMessageToOneFriend(
        message: String,
        friendId: String,
        fetchedChatId: String = ""
    ) {
        messageServiceRepository.sendMessageToSingleUser(message, friendId, fetchedChatId)

    }


    fun fetchMessage(
        friendId: String,
        fetchedChatId: String = "",
        onMessageFetched: (List<Message>) -> Unit
    ): ListenerRegistration? {

        val listener = messageServiceRepository.fetchMessages(friendId, fetchedChatId)
        { messages ->
            onMessageFetched(messages)
        }

        return listener
    }

    fun markAllMessageAsSeen(chatId: String) {
        val user = auth.currentUser
        if (user != null) {
            val currentUserId = user.uid
            messageServiceRepository.markMessageAsSeen(chatId, currentUserId)
        }
    }

    fun isCurrentUserASender(senderId: String): Boolean {
        val user = auth.currentUser

        return senderId == user?.uid

    }

    fun hasUnseenMessages(messages: List<Message>): Boolean {

        val currentUserId = auth.currentUser?.uid ?: return false
        return messages.any { it.receiverId == currentUserId && it.status == "delivered" }
    }


    private fun setOnlineStatus(status: Boolean = true) {
        onlineStatusRepo.setOnlineStatusWithDisconnect(status)
    }

    fun fetchOnlineStatus(userId: String, onStatusChanged: (Long) -> Unit) {
        authRepository.listenForOnlineStatus(userId)
        { onlineStatus ->
            onStatusChanged(onlineStatus)
        }
    }

    fun setCurrentOpenChatId(chatId: String?) {
        _currentOpenChatId.value = chatId
    }

    private fun startGlobalListener() {
        chatManager.startGlobalMessageListener(
            isUserInChatScreen = { chatId -> _currentOpenChatId.value == chatId },
        ) { chatList ->

            _activeChatList.value = chatList
        }

    }

    override fun onCleared() {
        chatManager.clearAllGlobalListeners()
        messageServiceRepository.clearMessageListeners()
        super.onCleared()
    }



}

sealed class AuthState {
    data object Authenticated : AuthState() // updated object to data object
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    //data object DoNothing : AuthState()
    data class Error(val message: String?) : AuthState()
}