package com.code4u.chatsapp.chat_feature.domain.message_use_case

import com.code4u.chatsapp.chat_feature.domain.repository.MessageHandlerRepo

class SendMessage (
    private val messageHandlerRepo: MessageHandlerRepo
) {
    
    suspend operator fun invoke(
        messageContent : String,
        receiverId : String,
        fetchedChatId : String,
        receiverName : String,
        userName : String
    ){
        
        messageHandlerRepo.sendMessageToSingleUser(
            messageText = messageContent,
            otherUserId = receiverId,
            fetchedChatId = fetchedChatId,
            friendName = receiverName,
            currentUsername = userName
        )
    }
}