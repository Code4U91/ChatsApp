package com.example.chatapp.chat_feature.data.remote_source.mapper

import com.example.chatapp.chat_feature.data.local_source.entity.MessageEntity
import com.example.chatapp.chat_feature.data.remote_source.model.MessageData


fun MessageData.toEntity() = MessageEntity(
    messageId = messageId,
    chatId = chatId,
    messageContent = messageContent,
    receiverId = receiverId,
    senderId = senderId,
    status = status,
    timeInMills = timeStamp?.toDate()?.time ?: 0L
)