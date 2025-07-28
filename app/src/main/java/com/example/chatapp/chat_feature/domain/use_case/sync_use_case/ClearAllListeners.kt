package com.example.chatapp.chat_feature.domain.use_case.sync_use_case

import com.example.chatapp.chat_feature.domain.repository.GlobalMessageListenerRepo
import com.example.chatapp.chat_feature.domain.repository.MessageHandlerRepo

class ClearAllListeners (
    private val globalMessageListenerRepo: GlobalMessageListenerRepo,
    private val messageHandlerRepo: MessageHandlerRepo
) {
    operator fun invoke(){

        globalMessageListenerRepo.clearAllGlobalListeners()
        messageHandlerRepo.clearMessageListeners()
    }
}