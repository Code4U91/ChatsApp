package com.example.chatapp.chat_feature.domain.repository

interface MessageHandlerRepo {

    suspend fun sendMessageToSingleUser(
        messageText: String,
        otherUserId: String,
        fetchedChatId: String,
        friendName: String,
        currentUsername: String
    )

    fun markMessageAsSeen(chatId: String, currentUserId: String, friendId: String)

    fun chatIdCreator(currentUserId: String, friendUserId: String): String


    fun deleteMessage(chatId: String, messageId: Set<String>, currentUserId: String)


}