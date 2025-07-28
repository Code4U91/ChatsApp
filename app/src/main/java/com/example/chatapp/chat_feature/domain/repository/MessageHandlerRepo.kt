package com.example.chatapp.chat_feature.domain.repository

import com.example.chatapp.core.FriendData
import com.example.chatapp.core.FriendListData
import com.example.chatapp.core.UserData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ListenerRegistration

interface MessageHandlerRepo {

    // profile feature
    fun fetchUserData(user: FirebaseUser, onDataChanged: (UserData?) -> Unit)

    fun fetchFriendData(
        friendUserId: String,
        updatedFriendData: (FriendData) -> Unit
    ): ListenerRegistration?

    fun addFriend(
        friendUserIdEmail: String, onSuccess: () -> Unit, onFailure: (e: Exception) -> Unit
    )

    fun addFriendByEmail(
        friendEmail: String,
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun addFriendById(
        friendId: String,
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun checkAndFriend(
        userId: String,
        friendId: String,
        friendName: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )


    fun fetchFriendList(onFriendUpdated: (List<FriendListData>) -> Unit): ListenerRegistration?


    suspend fun sendMessageToSingleUser(
        messageText: String,
        otherUserId: String,
        fetchedChatId: String,
        friendName: String?,
        currentUsername: String?
    )

    fun markMessageAsSeen(chatId: String, currentUserId: String, friendId: String)

    fun chatIdCreator(currentUserId: String, friendUserId: String, fetchedChatId: String): String

    suspend fun updateFcmTokenIfNeeded(savedTokens: List<String>)


    fun deleteMessage(chatId: String, messageId: Set<String>, currentUserId: String)

    fun deleteFriend(friendId: Set<String>)


    fun clearMessageListeners()
}