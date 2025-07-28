package com.example.chatapp.chat_feature.data.remote_source.mapper

import com.example.chatapp.chat_feature.data.remote_source.model.MessageData
import com.example.chatapp.chat_feature.domain.model.Message

fun MessageData.toDomain() = Message(
    messageId = messageId,
    chatId = chatId,
    messageContent = messageContent,
    receiverId = receiverId,
    senderId = senderId,
    status = status,
    timeStamp = timeStamp?.toDate()?.time ?: 0L
)