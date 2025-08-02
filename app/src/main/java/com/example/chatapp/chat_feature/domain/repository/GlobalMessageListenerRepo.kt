package com.example.chatapp.chat_feature.domain.repository

import com.example.chatapp.chat_feature.data.remote_source.repositoryImpl.GlobalChatEvent
import kotlinx.coroutines.flow.Flow

interface GlobalMessageListenerRepo {

    fun startGlobalMessageListener(
        isUserInChatScreen: (String) -> Boolean
    ): Flow<GlobalChatEvent>

    fun clearAllGlobalListeners()
}