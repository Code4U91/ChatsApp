package com.example.chatapp.chat_feature.data.remote_source.mapper

import com.example.chatapp.chat_feature.data.remote_source.model.ChatData
import com.example.chatapp.chat_feature.domain.model.Chat


fun ChatData.toDomain() = Chat(
    chatId = chatId,
    otherUserId = otherUserId,
    lastMessageTimeStamp = lastMessageTimeStamp?.toDate()?.time ?: 0L,
    otherUserName = otherUserName,
    profileUrl = profileUrl
)