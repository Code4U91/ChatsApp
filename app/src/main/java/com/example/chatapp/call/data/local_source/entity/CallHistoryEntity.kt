package com.example.chatapp.call.data.local_source.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("callHistory")
data class CallHistoryEntity(

    @PrimaryKey val callId: String,

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
