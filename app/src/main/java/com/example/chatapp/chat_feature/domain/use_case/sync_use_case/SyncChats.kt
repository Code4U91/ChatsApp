package com.example.chatapp.chat_feature.domain.use_case.sync_use_case

import com.example.chatapp.chat_feature.domain.repository.GlobalMessageListenerRepo
import com.example.chatapp.chat_feature.domain.repository.LocalChatRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SyncChats (
    private val globalMessageListenerRepo: GlobalMessageListenerRepo,
    private val localChatRepo: LocalChatRepo
) {

    operator fun invoke(
        scope : CoroutineScope,
        isUserInChatScreen  : (String) -> Boolean){

        globalMessageListenerRepo.startGlobalMessageListener(
            isUserInChatScreen = { chatId -> isUserInChatScreen(chatId) },
            onFetchAllActiveChat = { chatList ->

                scope.launch {
                    localChatRepo.insertChat(chatList)
                }

            },

            onNewMessages = { chatId , messages ->

                scope.launch {
                    localChatRepo.insertMessage(messages)
                }

            }
        )
    }
}