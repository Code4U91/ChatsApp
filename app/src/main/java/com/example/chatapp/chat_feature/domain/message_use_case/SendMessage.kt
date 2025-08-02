package com.example.chatapp.chat_feature.domain.message_use_case

import com.example.chatapp.chat_feature.domain.repository.MessageHandlerRepo

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