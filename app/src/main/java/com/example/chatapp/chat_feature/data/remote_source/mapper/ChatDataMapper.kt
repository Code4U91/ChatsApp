package com.example.chatapp.chat_feature.data.remote_source.mapper

import com.example.chatapp.chat_feature.data.local_source.entity.ChatEntity
import com.example.chatapp.chat_feature.data.remote_source.model.ChatData


fun ChatData.toEntity() = ChatEntity(
    chatId = chatId,
    otherUserId = otherUserId,
    lastMessageTimeInMills = lastMessageTimeStamp?.toDate()?.time ?: 0L,
    otherUserName = otherUserName,
    profileUrl = profileUrl
)