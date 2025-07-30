package com.example.chatapp.common.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.auth_feature.domain.usecase.auth_case.AuthUseCase
import com.example.chatapp.auth_feature.domain.usecase.online_state_case.OnlineStatusUseCase
import com.example.chatapp.call_feature.domain.usecase.call_history_case.CallHistoryUseCase
import com.example.chatapp.chat_feature.domain.model.Message
import com.example.chatapp.chat_feature.domain.repository.MessageHandlerRepo
import com.example.chatapp.chat_feature.domain.use_case.message_use_case.MessageUseCase
import com.example.chatapp.chat_feature.domain.use_case.sync_use_case.ChatsSyncAndUnSyncUseCase
import com.example.chatapp.core.FriendData
import com.example.chatapp.core.FriendListData
import com.example.chatapp.core.local_database.toUi
import com.example.chatapp.localData.roomDbCache.FriendEntity
import com.example.chatapp.localData.roomDbCache.LocalDbRepo
import com.example.chatapp.profile_feature.domain.use_case.UserDataUseCase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlobalMessageListenerViewModel @Inject constructor(

    private val messagingHandlerRepoImpl: MessageHandlerRepo,
    private val localDbRepo: LocalDbRepo,

    private val authUseCase: AuthUseCase,
    private val callHistoryUseCase: CallHistoryUseCase,
    private val chatsSyncAndUnSyncUseCase: ChatsSyncAndUnSyncUseCase,
    private val messageUseCase: MessageUseCase,
    private val onlineStatusUseCase: OnlineStatusUseCase,
    private val userDataUseCase : UserDataUseCase
) : ViewModel() {


    private val _currentOpenChatId = MutableStateFlow<String?>(null)
    val currentOpenChatId = _currentOpenChatId.asStateFlow()

    // done
    val activeChats = messageUseCase.getAllChats()
        .stateIn(
            viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // done
    val userData = userDataUseCase.getUserData()
        .stateIn(
            viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = null
        )

    val friendList = localDbRepo.friendList
        .map { friendEntities -> friendEntities.map { it.toUi() } }
        .stateIn(
            viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // done
    val callHistory = callHistoryUseCase.getCallHistoryUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    init {

        startGlobalListener()

        fetchUserData()

        setOnlineStatus() // -> marks true, online


        listenToRemoteCallHistory()

        viewModelScope.launch {
            userData.filterNotNull().collect { user ->
                messagingHandlerRepoImpl.updateFcmTokenIfNeeded(user.allFcmToken)
            }
        }

    }

    // done
    private fun startGlobalListener() {

        viewModelScope.launch {
            chatsSyncAndUnSyncUseCase.syncChats(
                isUserInChatScreen = { chatId -> _currentOpenChatId.value == chatId }
            )
        }
    }

    // done
    fun listenToRemoteCallHistory() {
        viewModelScope.launch {
            callHistoryUseCase.syncCallHistoryUseCase()
        }
    }


    // ROOM DB FUNCTIONS ---- START


    // done
    fun getMessage(chatId: String): Flow<List<Message>> {

        return messageUseCase.getMessage(chatId)

    }

    fun getFriendData(friendId: String): Flow<FriendData?> {

        return localDbRepo.getFriendData(friendId)
            .map { it?.toUi() }
    }


    fun insertFriend(friendEntity: FriendEntity) = viewModelScope.launch {

        localDbRepo.insertFriend(friendEntity)
    }


    // ROOM DB FUNCTION -------- END


    // done
    private fun fetchUserData() {

        viewModelScope.launch {
            userDataUseCase.syncUserData()
        }

    }

    fun fetchFriendList(onFriendUpdated: (List<FriendListData>) -> Unit): ListenerRegistration? {

        return messagingHandlerRepoImpl.fetchFriendList { friendListData ->

            onFriendUpdated(friendListData)
        }
    }

    fun fetchFriendData(
        friendUserId: String,
        updatedFriendData: (FriendData) -> Unit
    ): ListenerRegistration? {

        return messagingHandlerRepoImpl.fetchFriendData(friendUserId)
        {

            updatedFriendData(it) // used where multiple new friend data is required at once

        }
    }

    fun addNewFriend(
        friendUserId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        messagingHandlerRepoImpl.addFriend(
            friendUserId,
            onSuccess = { onSuccess() },
            onFailure = { e -> onFailure(e) }
        )
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

    fun deleteFriend(friendId: Set<String>) {
        messagingHandlerRepoImpl.deleteFriend(friendId)
    }

    fun markAllMessageAsSeen(chatId: String, friendId: String) {

        authUseCase.getCurrentUser()?.let {
            messagingHandlerRepoImpl.markMessageAsSeen(chatId, it.uid, friendId)
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

            authUseCase.signOutUseCase()
            delay(100)
            localDbRepo.clearAllTables()

        }

    }


    override fun onCleared() {

        Log.i("TimesEx", "OnDestroy")
        super.onCleared()
    }
}