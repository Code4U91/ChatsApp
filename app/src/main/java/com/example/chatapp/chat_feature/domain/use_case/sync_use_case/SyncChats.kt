package com.example.chatapp.chat_feature.domain.use_case.sync_use_case

import com.example.chatapp.auth_feature.domain.usecase.auth_case.AuthUseCase
import com.example.chatapp.chat_feature.data.remote_source.repositoryImpl.GlobalChatEvent
import com.example.chatapp.chat_feature.domain.repository.GlobalMessageListenerRepo
import com.example.chatapp.chat_feature.domain.repository.LocalChatRepo
import kotlinx.coroutines.flow.distinctUntilChanged

class SyncChats(
    private val globalMessageListenerRepo: GlobalMessageListenerRepo,
    private val localChatRepo: LocalChatRepo,
    private val authUseCase: AuthUseCase
) {

    suspend operator fun invoke(isUserInChatScreen: (String) -> Boolean) {

        authUseCase.getCurrentUser()?.let {

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
}