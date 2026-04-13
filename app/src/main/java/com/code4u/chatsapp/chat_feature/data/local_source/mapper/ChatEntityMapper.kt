package com.code4u.chatsapp.chat_feature.data.local_source.mapper

import com.code4u.chatsapp.chat_feature.data.local_source.entity.ChatEntity
import com.code4u.chatsapp.chat_feature.domain.model.Chat

fun ChatEntity.toDomain() = Chat(
    chatId = chatId,
    otherUserId = otherUserId,
    lastMessageTimeInMills = lastMessageTimeInMills,
    otherUserName = otherUserName,
    profileUrl = profileUrl
)

fun Chat.toEntity() = ChatEntity(
    chatId = chatId,
    otherUserId = otherUserId,
    lastMessageTimeInMills = lastMessageTimeInMills,
    otherUserName = otherUserName,
    profileUrl = profileUrl
)