package com.example.chatapp.chat_feature.data.remote_source.model

import kotlinx.serialization.Serializable

@Serializable
data class MessageNotificationRequest(
    val senderId: String,
    val receiverId: String,
    val messageId: String,
    val chatId: String

)
