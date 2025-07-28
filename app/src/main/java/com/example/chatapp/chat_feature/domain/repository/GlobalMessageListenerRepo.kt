package com.example.chatapp.chat_feature.domain.repository

import com.example.chatapp.chat_feature.domain.model.Chat
import com.example.chatapp.chat_feature.domain.model.Message

interface GlobalMessageListenerRepo {

    fun startGlobalMessageListener(
        isUserInChatScreen: (String) -> Boolean,
        onFetchAllActiveChat: (List<Chat>) -> Unit,
        onNewMessages: (String, List<Message>) -> Unit
    )

    fun clearAllGlobalListeners()
}