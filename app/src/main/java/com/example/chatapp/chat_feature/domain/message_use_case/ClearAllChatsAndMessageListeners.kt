package com.example.chatapp.chat_feature.domain.message_use_case

import com.example.chatapp.chat_feature.domain.repository.GlobalMessageListenerRepo

class ClearAllChatsAndMessageListeners(
    private val globalMessageListenerRepo: GlobalMessageListenerRepo
) {

    operator fun invoke(){

        globalMessageListenerRepo.clearAllGlobalListeners()
    }
}