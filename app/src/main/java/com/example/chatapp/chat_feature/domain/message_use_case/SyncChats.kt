package com.example.chatapp.chat_feature.domain.message_use_case

import com.example.chatapp.chat_feature.data.remote_source.repositoryImpl.GlobalChatEvent
import com.example.chatapp.chat_feature.domain.repository.GlobalMessageListenerRepo
import com.example.chatapp.chat_feature.domain.repository.LocalChatRepo
import kotlinx.coroutines.flow.distinctUntilChanged

class SyncChats(
    private val globalMessageListenerRepo: GlobalMessageListenerRepo,
    private val localChatRepo: LocalChatRepo
) {

    suspend operator fun invoke(isUserInChatScreen: (String) -> Boolean) {

        globalMessageListenerRepo.startGlobalMessageListener(
            isUserInChatScreen = { chatId -> isUserInChatScreen(chatId) }
        ).distinctUntilChanged()
            .collect { event ->

                when (event) {
                    is GlobalChatEvent.AllChatsFetched -> {
                        localChatRepo.insertChat(event.chatList)
                    }

                    is GlobalChatEvent.NewMessages -> {
                        localChatRepo.insertMessage(event.messages)
                    }
                }

            }
    }
}