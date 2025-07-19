package com.example.chatapp.call_feature.domain.model

data class Call (

    val callId: String,
    val callerId: String,
    val callReceiverId: String,
    val callType: String,
    val channelId: String,
    val status: String,
    val callStartTime: Long,
    val callEndTime: Long,
    val otherUserName: String,
    val otherUserId: String
)