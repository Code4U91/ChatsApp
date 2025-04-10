package com.example.chatapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.CallData
import com.example.chatapp.ChatItemData
import com.example.chatapp.FriendData
import com.example.chatapp.FriendListData
import com.example.chatapp.Message
import com.example.chatapp.UserData
import com.example.chatapp.repository.ChatManager
import com.example.chatapp.repository.MessageServiceRepository
import com.example.chatapp.repository.OnlineStatusRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlobalMessageListenerViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val messageServiceRepository: MessageServiceRepository,
    private val chatManager: ChatManager,
    private val onlineStatusRepo: OnlineStatusRepo
) : ViewModel() {

    private val _activeChatList = MutableStateFlow<List<ChatItemData>>(emptyList())
    val activeChatList = _activeChatList.asStateFlow()

    private val _currentOpenChatId = MutableStateFlow<String?>(null)
    val currentOpenChatId = _currentOpenChatId.asStateFlow()

    private val _chatMessages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())

    val chatMessages = _chatMessages.asStateFlow()

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData = _userData.asStateFlow()

    private val _totalFriend = MutableStateFlow(0)
    val totalFriend: StateFlow<Int> = _totalFriend

    private val _friendData = MutableStateFlow<FriendData?>(null)
    val friendData: StateFlow<FriendData?> = _friendData

    private val _incomingCallData = MutableStateFlow<CallData?>(null)
    val incomingCallData = _incomingCallData.asStateFlow()

    private val _callHistoryData = MutableStateFlow<List<CallData>>(emptyList())
    val callHistoryData = _callHistoryData.asStateFlow()



    init {

        Log.i("TimesExecuted", "Executed")

        startGlobalListener()
        fetchUserData()
        setOnlineStatus() // -> marks true, online
        startCallListener()

        fetchCallHistory()

        viewModelScope.launch {
            userData.filterNotNull().collect { user ->
                messageServiceRepository.updateFcmTokenIfNeeded(user.fcmTokens)
            }
        }

    }

    private fun startGlobalListener() {
        chatManager.startGlobalMessageListener(
            isUserInChatScreen = { chatId -> _currentOpenChatId.value == chatId },
            onFetchAllActiveChat = { chatList ->

                _activeChatList.value = chatList

            },
            onNewMessages = { chatId, messages ->

                viewModelScope.launch {
                    delay(200) // to reduce read spikes, may increase more or decreased
                    _chatMessages.value = _chatMessages.value.toMutableMap().apply {
                        put(chatId, messages)
                    }
                }

            }
        )

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

    fun fetchFriendList(onFriendUpdated: (List<FriendListData>) -> Unit): ListenerRegistration? {

        return messageServiceRepository.fetchFriendList { friendDocument, updatedTotalFriend ->
            onFriendUpdated(friendDocument)
            _totalFriend.value = updatedTotalFriend
        }
    }

    fun fetchFriendData(
        friendUserId: String,
        updatedFriendData: (FriendData?) -> Unit
    ): ListenerRegistration {

        return messageServiceRepository.fetchFriendData(friendUserId)
        {
            updatedFriendData(it) // used where multiple new friend data is required at once

            if (friendData.value?.uid == friendUserId) { // only update this if both id same
                _friendData.value = it  // this one has same friend data at all place
            }

        }

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

    private fun setOnlineStatus(status: Boolean = true) {
        onlineStatusRepo.setOnlineStatusWithDisconnect(status)
    }

    fun setCurrentOpenChatId(chatId: String?) {
        if (_currentOpenChatId.value != chatId)
        {
            _currentOpenChatId.value = chatId
        }

    }

    fun updateFriendName(friendName: String, friendId: String, whichList: String, chatId: String) {
        val userID = auth.currentUser?.uid

        if (whichList == "friendList") {
            userID?.let { currentUserId ->
                messageServiceRepository.updateFriendNameOnFriendList(
                    friendName,
                    currentUserId,
                    friendId
                )
            }
        } else {
            messageServiceRepository.updateFriendNameOnChatList(friendName, friendId, chatId)
        }

    }

    fun calculateChatId(otherId: String): String {

        val currentUserId = auth.currentUser?.uid
        currentUserId?.let {
            return messageServiceRepository.chatIdCreator(it, otherId, "")
        }

        return ""
    }

    fun fetchOnlineStatus(
        userId: String,
        onStatusChanged: (Long) -> Unit
    ): Pair<DatabaseReference, ValueEventListener> {
        return onlineStatusRepo.listenForOnlineStatus(userId) { onlineStatus ->
            onStatusChanged(onlineStatus)
        }
    }

    fun deleteFriend(friendId: String) {
        messageServiceRepository.deleteFriend(friendId)
    }

    fun updateFriendData(friendData: FriendData) {
        _friendData.value = friendData

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

    fun sendMessageToOneFriend(
        message: String,
        friendId: String,
        fetchedChatId: String = ""
    ) {
        messageServiceRepository.sendMessageToSingleUser(message, friendId, fetchedChatId)

    }

    private fun startCallListener()
    {
        messageServiceRepository.callListener { callData ->
            _incomingCallData.value = callData
        }
    }

    fun emptyIncomingCall()
    {
        _incomingCallData.value = null
    }

    private fun fetchCallHistory()
    {

        messageServiceRepository.fetchCallHistory { callList ->

            _callHistoryData.value = callList

        }

    }


    override fun onCleared() {

        Log.i("TimesEx", "OnDestroy")
        chatManager.clearAllGlobalListeners()
        messageServiceRepository.clearMessageListeners()
        super.onCleared()
    }
}