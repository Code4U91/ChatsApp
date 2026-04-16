package com.code4u.chatsapp.chat_feature.domain.message_use_case

import com.code4u.chatsapp.chat_feature.domain.repository.GlobalMessageListenerRepo

class ClearAllChatsAndMessageListeners(
    private val globalMessageListenerRepo: GlobalMessageListenerRepo
) {

    operator fun invoke(){

        globalMessageListenerRepo.clearAllGlobalListeners()
    }
}