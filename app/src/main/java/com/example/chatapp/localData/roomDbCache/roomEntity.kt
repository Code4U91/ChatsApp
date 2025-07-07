package com.example.chatapp.localData.roomDbCache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("user")
data class UserEntity(
    @PrimaryKey val uid: String,

    val name: String,
    val photoUrl: String,
    val email: String,
    val about: String,

    )

@Entity("chats")
data class ChatEntity(

    @PrimaryKey val chatId: String,

    val otherUserId: String,
    val lastMessageTimeStamp: Long,
    val otherUserName: String,
    val profileUrl : String
)

@Entity("messages")
data class MessageEntity(

    @PrimaryKey val messageId: String,

    val chatId: String,
    val messageContent: String,
    val receiverId: String,
    val senderId: String,
    val status: String,
    val timeStamp: Long


)

@Entity("friends")
data class FriendEntity(

    @PrimaryKey val friendId: String,

    val friendName: String,
    val photoUrl: String,
    val about: String

)

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