package com.example.chatapp.core.local_database

import com.example.chatapp.call.data.local_source.entity.CallHistoryEntity
import com.example.chatapp.call.data.remote_source.model.CallData
import com.example.chatapp.core.ChatItemData
import com.example.chatapp.core.FriendData
import com.example.chatapp.core.FriendListData
import com.example.chatapp.core.Message
import com.example.chatapp.core.UserData
import com.example.chatapp.localData.roomDbCache.ChatEntity
import com.example.chatapp.localData.roomDbCache.FriendEntity
import com.example.chatapp.localData.roomDbCache.MessageEntity
import com.example.chatapp.localData.roomDbCache.UserEntity
import com.google.firebase.Timestamp

fun ChatItemData.toEntity(): ChatEntity {
    return ChatEntity(
        chatId = this.chatId,
        otherUserId = this.otherUserId.orEmpty(),
        otherUserName = this.otherUserName,
        profileUrl = this.profileUrl,
        lastMessageTimeStamp = this.lastMessageTimeStamp?.toDate()?.time ?: 0L
    )
}

fun ChatEntity.toUi(): ChatItemData {
    return ChatItemData(
        chatId = this.chatId,
        otherUserId = this.otherUserId,
        otherUserName = this.otherUserName,
        lastMessageTimeStamp = Timestamp(this.lastMessageTimeStamp / 1000, 0),
        profileUrl = this.profileUrl
    )
}

fun FriendData.toEntity(): FriendEntity {
    return FriendEntity(
        friendId = this.uid,
        friendName = this.name,
        photoUrl = this.photoUrl,
        about = this.about
    )
}

fun FriendEntity.toUi() : FriendData {
    return FriendData(
        uid = this.friendId,
        name = this.friendName,
        photoUrl = this.photoUrl,
        about = this.about
    )
}

fun FriendListData.toEntity() : FriendEntity {
    return FriendEntity(
        friendId = this.friendId,
        friendName = this.friendName,
        photoUrl = "",
        about = ""
    )
}

fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        messageId = this.messageId,
        chatId = this.chatId,
        messageContent = this.messageContent.orEmpty(),
        receiverId = this.receiverId,
        senderId = this.senderId,
        status = this.status,
        timeStamp = this.timeStamp?.toDate()?.time ?: 0L
    )
}

fun MessageEntity.toUi(): Message {
    return Message(
        messageId = this.messageId,
        chatId = this.chatId,
        messageContent = this.messageContent,
        receiverId = this.receiverId,
        senderId = this.senderId,
        status = this.status,
        timeStamp = Timestamp(this.timeStamp / 1000, 0)
    )
}

fun UserData.toEntity(): UserEntity {
    return UserEntity(
        uid = this.uid,
        name = this.name,
        photoUrl = this.photoUrl,
        email = this.email,
        about = this.about
    )
}

fun UserEntity.toUi() : UserData {
    return UserData(
        uid = this.uid,
        name = this.name,
        photoUrl = this.photoUrl,
        email = this.email,
        about = this.about
    )
}

fun CallData.toEntity() : CallHistoryEntity {

    return CallHistoryEntity(
        callId = this.callId,
        callerId = this.callerId,
        callReceiverId = this.callReceiverId,
        callType = this.callType,
        channelId = this.channelId,
        status = this.status,
        callStartTime = this.callStartTime?.toDate()?.time ?: 0L,
        callEndTime = this.callEndTime?.toDate()?.time ?: 0L,
        otherUserName = this.otherUserName,
        otherUserId = this.otherUserId
    )
}

fun CallHistoryEntity.toUi() : CallData {

    return CallData(
        callId = this.callId,
        callerId = this.callerId,
        callReceiverId = this.callReceiverId,
        callType = this.callType,
        channelId = this.channelId,
        status = this.status,
        callStartTime = Timestamp(this.callStartTime / 1000, 0),
        callEndTime = Timestamp(this.callEndTime / 1000, 0),
        otherUserName = this.otherUserName,
        otherUserId = this.otherUserId
    )
}

