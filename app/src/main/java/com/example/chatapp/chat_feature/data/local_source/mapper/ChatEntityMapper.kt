package com.example.chatapp.chat_feature.data.local_source.mapper

import com.example.chatapp.chat_feature.data.local_source.entity.ChatEntity
import com.example.chatapp.chat_feature.domain.model.Chat

fun ChatEntity.toDomain() = Chat(
    chatId = chatId,
    otherUserId = otherUserId,
    lastMessageTimeStamp = lastMessageTimeStamp,
    otherUserName = otherUserId,
    profileUrl = profileUrl
)

fun Chat.toEntity() = ChatEntity(
    chatId = chatId,
    otherUserId = otherUserId,
    lastMessageTimeStamp = lastMessageTimeStamp,
    otherUserName = otherUserName,
    profileUrl = profileUrl
)