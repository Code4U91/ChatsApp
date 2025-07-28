package com.example.chatapp.chat_feature.data.remote_source.model

import com.google.firebase.Timestamp

data class MessageData(
    val messageContent: String = "",
    val messageId: String = "",
    val receiverId: String = "",
    val senderId: String = "",
    val status: String = "",  // sending -> sent -> delivered -> seen
    val timeStamp: Timestamp? = null,
    val chatId: String = ""
)
