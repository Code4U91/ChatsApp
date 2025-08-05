package com.example.chatapp.shared.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.auth_feature.domain.usecase.auth_case.AuthUseCase
import com.example.chatapp.auth_feature.domain.usecase.online_state_case.OnlineStatusUseCase
import com.example.chatapp.call_feature.domain.usecase.call_history_case.CallHistoryUseCase
import com.example.chatapp.chat_feature.domain.message_use_case.MessageUseCase
import com.example.chatapp.chat_feature.domain.model.Message
import com.example.chatapp.friend_feature.domain.model.Friend
import com.example.chatapp.friend_feature.domain.use_case.FriendUseCase
import com.example.chatapp.profile_feature.domain.use_case.UserDataUseCase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlobalMessageListenerViewModel @Inject constructor(

    private val authUseCase: AuthUseCase,
    private val callHistoryUseCase: CallHistoryUseCase,
    private val messageUseCase: MessageUseCase,
    private val onlineStatusUseCase: OnlineStatusUseCase,
    private val userDataUseCase: UserDataUseCase,
    private val friendUseCase: FriendUseCase

) : ViewModel() {


    private val _currentOpenChatId = MutableStateFlow<String?>(null)
    val currentOpenChatId = _currentOpenChatId.asStateFlow()

    private val messageFlows = mutableMapOf<String, StateFlow<List<Message>>>()
    private val friendFlows = mutableMapOf<String, StateFlow<Friend?>>()


    val activeChats = messageUseCase.getAllChats()
        .stateIn(
            viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    val userData = userDataUseCase.getUserData()
        .stateIn(
            viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = null
        )

    val friendList = friendUseCase.getFriendList()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    val callHistory = callHistoryUseCase.getCallHistoryUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    init {

        startGlobalListener()

        fetchUserData()

        syncFriendData()

        setOnlineStatus() // -> marks true, online


        listenToRemoteCallHistory()

        viewModelScope.launch {
            userData.filterNotNull().collect { user ->
                authUseCase.updateFcmTokenIfNeeded(user.allFcmToken)
            }
        }

    }


    private fun startGlobalListener() {

        viewModelScope.launch {
             messageUseCase.syncChats(
                isUserInChatScreen = { chatId -> _currentOpenChatId.value == chatId }
            )
        }
    }


    fun listenToRemoteCallHistory() {
        viewModelScope.launch {
            callHistoryUseCase.syncCallHistoryUseCase()
        }
    }




    fun getOrFetchFriend(friendId: String) : StateFlow<Friend?> {

        return friendFlows.getOrPut(friendId) {
            friendUseCase.getFriendDataById(friendId)
                .onEach { friend ->
                    if (friend == null){
                        addNewFriend(friendId, {}, {
                            e ->
                            Log.i("FRIEND_ADD", e)
                        })
                        Log.i("FRIEND_ADD", "times called")
                    }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Companion.WhileSubscribed(5000),
                    initialValue = null
                )
        }
    }

    fun getMessage(chatId: String): Flow<List<Message>> {

        return messageFlows.getOrPut(chatId){
            messageUseCase.getMessage(chatId)
                .stateIn(
                    viewModelScope,
                    SharingStarted.Companion.WhileSubscribed(5000),
                    emptyList()
                )
        }
    }


    private fun fetchUserData() {

        viewModelScope.launch {
            userDataUseCase.syncUserData()
        }

    }

    fun syncFriendData() = viewModelScope.launch {

        friendUseCase.syncFriendData()
    }

    fun addNewFriend(
        friendUserId: String,
        onSuccess: () -> Unit,
        onFailure: (message: String) -> Unit
    ) {

        friendUseCase.addFriend(
            friendUserId,
            onSuccess = { onSuccess() },
            onFailure = { e -> onFailure(e.message.orEmpty()) })

    }

    private fun setOnlineStatus(status: Boolean = true) {

        onlineStatusUseCase.setOnlineStatus(status)
    }

    fun setCurrentOpenChatId(chatId: String?) {
        if (_currentOpenChatId.value != chatId) {
            _currentOpenChatId.value = chatId
        }

    }

    fun calculateChatId(otherId: String): String? {

        return authUseCase.getCurrentUser()?.let { user ->
            messageUseCase.calculateChatId(user.uid, otherId)
        }
    }

    fun fetchOnlineStatus(
        userId: String,
        onStatusChanged: (Long) -> Unit
    ): Pair<DatabaseReference, ValueEventListener> {

        return onlineStatusUseCase.listenForOnlineStatus(
            userId,
            onStatusChanged = { onlineStatus ->
                onStatusChanged(onlineStatus)
            },
        )

    }

    fun setActiveChat(chatId: String) {

        onlineStatusUseCase.setActiveChatUseCase(chatId)
    }

    fun deleteFriend(friendIds: Set<String>) {

        viewModelScope.launch {
            friendUseCase.deleteFriend(friendIds)
        }

    }

    fun markAllMessageAsSeen(chatId: String, friendId: String) {

        authUseCase.getCurrentUser()?.let {
            messageUseCase.markMessageAsSeen(chatId, it.uid, friendId)
        }
    }

    fun isCurrentUserASender(senderId: String): Boolean {


        val user = authUseCase.getCurrentUser()

        return senderId == user?.uid

    }

    fun hasUnseenMessages(messages: List<Message>): Boolean {

        val currentUserId = authUseCase.getCurrentUser()?.uid ?: return false
        return messages.any { it.receiverId == currentUserId && it.status == "delivered" }
    }

    fun sendMessageToOneFriend(
        message: String,
        friendId: String,
        fetchedChatId: String = "",
        friendName: String,
        currentUsername: String,
    ) {
        viewModelScope.launch {

            messageUseCase.sendMessage(
                messageContent = message,
                receiverId = friendId,
                fetchedChatId = fetchedChatId,
                receiverName = friendName,
                userName = currentUsername
            )
        }

    }


    fun deleteMessage(chatId: String, messageId: Set<String>) {

        viewModelScope.launch {
            authUseCase.getCurrentUser()?.let {
                messageUseCase.deleteMessages(chatId, messageId, it.uid)
            }
        }


    }


    // signing out from this viewmodel instead of chatsViewModel because
    // there's minute delay between the stopping of listener (userData) when using signOut() from
    // chatsViewModel which then causes room to re-fill the user data just after logout is clicked
    fun signOut() {

        viewModelScope.launch {

            messageUseCase.clearAllChatsAndMessageListeners()
            userDataUseCase.clearUserDataListener()
            callHistoryUseCase.clearCallHistoryListener()
            authUseCase.signOutUseCase()
            delay(300)
            userDataUseCase.clearLocalDbUseCase()

        }

    }


    override fun onCleared() {

        Log.i("TimesEx", "OnDestroy")
        super.onCleared()
    }
}