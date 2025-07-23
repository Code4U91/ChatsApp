package com.example.chatapp.common.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.auth_feature.domain.repository.AuthRepository
import com.example.chatapp.auth_feature.domain.repository.OnlineStatusRepo
import com.example.chatapp.call_feature.domain.model.Call
import com.example.chatapp.call_feature.domain.usecase.call_history_case.CallHistoryUseCase
import com.example.chatapp.call_feature.presentation.mapper.toUi
import com.example.chatapp.chat_feature.MessagingHandlerRepo
import com.example.chatapp.common.GlobalMessageListenerRepo
import com.example.chatapp.core.ChatItemData
import com.example.chatapp.core.FriendData
import com.example.chatapp.core.FriendListData
import com.example.chatapp.core.Message
import com.example.chatapp.core.UserData
import com.example.chatapp.core.local_database.toEntity
import com.example.chatapp.core.local_database.toUi
import com.example.chatapp.localData.roomDbCache.FriendEntity
import com.example.chatapp.localData.roomDbCache.LocalDbRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlobalMessageListenerViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val messagingHandlerRepo: MessagingHandlerRepo,
    private val globalMessageListenerRepo: GlobalMessageListenerRepo,
    private val onlineStatusRepoIml: OnlineStatusRepo,
    private val localDbRepo: LocalDbRepo,
    private val authRepositoryIml: AuthRepository,
    private val callHistoryUseCase: CallHistoryUseCase
) : ViewModel() {


    private val _currentOpenChatId = MutableStateFlow<String?>(null)
    val currentOpenChatId = _currentOpenChatId.asStateFlow()

    val activeChats = localDbRepo.chats
        .map { entityList -> entityList.map { it.toUi() } }
        .stateIn(
            viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userData = localDbRepo.userData
        .map { userEntity -> userEntity?.toUi() }
        .stateIn(viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = null
        )

    val friendList = localDbRepo.friendList
        .map { friendEntities -> friendEntities.map { it.toUi() } }
        .stateIn(viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList())

    val callHistory =  callHistoryUseCase.getCallHistoryUseCase(true)
        .map { call -> call.map { it.toUi() } }
        .stateIn(viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList())



    init {

        Log.i("TimesExecuted", "Executed")

        startGlobalListener()
        fetchUserData()
        setOnlineStatus() // -> marks true, online

       // fetchCallHistory()
         listenToRemoteCallHistory()

        viewModelScope.launch {
            userData.filterNotNull().collect { user ->
                messagingHandlerRepo.updateFcmTokenIfNeeded(user.fcmTokens)
            }
        }

    }

    private fun startGlobalListener() {
        globalMessageListenerRepo.startGlobalMessageListener(
            isUserInChatScreen = { chatId -> _currentOpenChatId.value == chatId },
            onFetchAllActiveChat = { chatList ->

                insertChats(chatList)

            },
            onNewMessages = { chatId, messages ->

                viewModelScope.launch {
                    delay(200)
                    messages.forEach {
                        insertMessage(it)
                    }

                }

            }
        )

    }

    fun  listenToRemoteCallHistory () {
        viewModelScope.launch {
            callHistoryUseCase.getCallHistoryUseCase(local = false)
                .distinctUntilChanged()
                .collect { callList ->
                    Log.i("HISTORY_CHECK", callList.toString())
                    insertCallHistory(callList)
                }
        }
    }




    // ROOM DB FUNCTIONS ---- START
    fun insertChats(chats: List<ChatItemData>) = viewModelScope.launch {

        chats.forEach {
            localDbRepo.insertChats(it.toEntity())
        }

    }



    fun getMessage(chatId: String): Flow<List<Message>> {
        return localDbRepo.getMessages(chatId)
            .map { it.map { entity -> entity.toUi() } }
    }

    fun getFriendData(friendId: String) : Flow<FriendData?> {

        return localDbRepo.getFriendData(friendId)
            .map { it?.toUi() }
    }

    suspend fun insertMessage(message: Message) {
        localDbRepo.insertMessages(message.toEntity())
    }

    private suspend fun insertCallHistory(callList : List<Call>){

        callHistoryUseCase.insertCallHistoryCase(callList)
    }


    fun insertFriend(friendEntity: FriendEntity) = viewModelScope.launch {

        localDbRepo.insertFriend(friendEntity)
    }


    fun insertUserData(userData: UserData) = viewModelScope.launch {
        localDbRepo.insertUserData(userData.toEntity())
    }

    // ROOM DB FUNCTION -------- END


    private fun fetchUserData() {

        auth.currentUser?.let {

            messagingHandlerRepo.fetchUserData(it)
            { updatedUserData ->

                updatedUserData?.let { data ->
                    insertUserData(data)
                }
            }
        }
    }

    fun fetchFriendList(onFriendUpdated: (List<FriendListData>) -> Unit): ListenerRegistration? {

        return messagingHandlerRepo.fetchFriendList { friendListData ->

            onFriendUpdated(friendListData)
        }
    }

    fun fetchFriendData(
        friendUserId: String,
        updatedFriendData: (FriendData) -> Unit
    ): ListenerRegistration? {

        return messagingHandlerRepo.fetchFriendData(friendUserId)
        {

            updatedFriendData(it) // used where multiple new friend data is required at once

        }
    }

    fun addNewFriend(
        friendUserId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        messagingHandlerRepo.addFriend(
            friendUserId,
            onSuccess = { onSuccess() },
            onFailure = { e -> onFailure(e) }
        )
    }

    private fun setOnlineStatus(status: Boolean = true) {
        onlineStatusRepoIml.setOnlineStatusWithDisconnect(status)
    }

    fun setCurrentOpenChatId(chatId: String?) {
        if (_currentOpenChatId.value != chatId) {
            _currentOpenChatId.value = chatId
        }

    }

    fun calculateChatId(otherId: String): String? {

        val currentUserId = auth.currentUser?.uid
        return currentUserId?.let {
            messagingHandlerRepo.chatIdCreator(it, otherId, "")
        }
    }

    fun fetchOnlineStatus(
        userId: String,
        onStatusChanged: (Long) -> Unit
    ): Pair<DatabaseReference, ValueEventListener> {
        return onlineStatusRepoIml.listenForOnlineStatus(userId) { onlineStatus ->
            onStatusChanged(onlineStatus)
        }
    }

    fun setActiveChat(chatId: String) {
        onlineStatusRepoIml.activeChatUpdate(chatId)
    }

    fun deleteFriend(friendId: Set<String>) {
        messagingHandlerRepo.deleteFriend(friendId)
    }

    fun markAllMessageAsSeen(chatId: String, friendId: String) {

        auth.currentUser?.uid?.let { currentUserId ->

            messagingHandlerRepo.markMessageAsSeen(chatId, currentUserId, friendId)
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
        fetchedChatId: String = "",
        friendName: String?,
        currentUsername: String?,
    ) {
        viewModelScope.launch {
            messagingHandlerRepo.sendMessageToSingleUser(
                message,
                friendId,
                fetchedChatId,
                friendName,
                currentUsername,
            )
        }

    }


    fun deleteMessage(chatId: String, messageId: Set<String>) {

        auth.currentUser?.uid?.let{
            messagingHandlerRepo.deleteMessage(chatId, messageId, it)
        }

    }


    // signing out from this viewmodel instead of chatsViewModel because
    // there's minute delay between the stopping of listener (userData) when using signOut() from
    // chatsViewModel which then causes room to re-fill the user data just after logout is clicked
    fun signOut() {

        viewModelScope.launch {

            globalMessageListenerRepo.clearAllGlobalListeners()
            messagingHandlerRepo.clearMessageListeners()
            authRepositoryIml.signOut()
            delay(100)
            localDbRepo.clearAllTables()


        }

    }


    override fun onCleared() {

        Log.i("TimesEx", "OnDestroy")
        globalMessageListenerRepo.clearAllGlobalListeners()
        messagingHandlerRepo.clearMessageListeners()
        super.onCleared()
    }
}