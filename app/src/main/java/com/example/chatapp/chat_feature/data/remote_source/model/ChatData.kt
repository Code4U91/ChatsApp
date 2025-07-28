package com.example.chatapp.chat_feature.data.remote_source.model

import com.google.firebase.Timestamp

data class ChatData(
    val chatId: String = "",
    val otherUserId: String = "",
    val lastMessageTimeStamp: Timestamp? = null,
    val otherUserName: String = "",
    val profileUrl : String = ""
)
