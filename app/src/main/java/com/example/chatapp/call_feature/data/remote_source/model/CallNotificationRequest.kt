package com.example.chatapp.call_feature.data.remote_source.model

import kotlinx.serialization.Serializable

@Serializable
data class CallNotificationRequest(
    val callId: String,
    val channelName: String,
    val callType: String,
    val senderId: String,
    val receiverId: String
)
