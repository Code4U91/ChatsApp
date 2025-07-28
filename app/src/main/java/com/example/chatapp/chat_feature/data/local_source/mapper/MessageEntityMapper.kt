package com.example.chatapp.chat_feature.data.local_source.mapper

import com.example.chatapp.chat_feature.data.local_source.entity.MessageEntity
import com.example.chatapp.chat_feature.domain.model.Message

fun MessageEntity.toDomain() = Message(
    messageId = messageId,
    chatId = chatId,
    messageContent = messageContent,
    receiverId = receiverId,
    senderId = senderId,
    status = status,
    timeStamp = timeStamp
)

fun Message.toEntity() = MessageEntity(
    messageId = messageId,
    chatId = chatId,
    messageContent = messageContent,
    receiverId = receiverId,
    senderId = senderId,
    status = status,
    timeStamp = timeStamp
)