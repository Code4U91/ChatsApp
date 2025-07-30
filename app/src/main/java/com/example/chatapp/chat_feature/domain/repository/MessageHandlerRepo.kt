package com.example.chatapp.chat_feature.domain.repository

import com.example.chatapp.core.FriendData
import com.example.chatapp.core.FriendListData
import com.google.firebase.firestore.ListenerRegistration

interface MessageHandlerRepo {

    // profile feature module
    //fun fetchUserData(user: FirebaseUser, onDataChanged: (UserData?) -> Unit)

    // friend feature module
    fun fetchFriendData(
        friendUserId: String,
        updatedFriendData: (FriendData) -> Unit
    ): ListenerRegistration?

    fun addFriend(
        friendUserIdEmail: String, onSuccess: () -> Unit, onFailure: (e: Exception) -> Unit
    )

    fun fetchFriendList(onFriendUpdated: (List<FriendListData>) -> Unit): ListenerRegistration?


    suspend fun sendMessageToSingleUser(
        messageText: String,
        otherUserId: String,
        fetchedChatId: String,
        friendName: String,
        currentUsername: String
    )

    fun markMessageAsSeen(chatId: String, currentUserId: String, friendId: String)

    fun chatIdCreator(currentUserId: String, friendUserId: String): String

    suspend fun updateFcmTokenIfNeeded(savedTokens: List<String>)


    fun deleteMessage(chatId: String, messageId: Set<String>, currentUserId: String)

    fun deleteFriend(friendId: Set<String>)

}