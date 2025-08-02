package com.example.chatapp.core

import android.os.Parcelable
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


data class ProfileItem(
    val primaryIcon: ImageVector,
    val secondaryIcon: ImageVector,
    val itemDescription: String,
    val itemValue: String
)

data class FriendScreenUiItem(
    val icon: ImageVector,
    val itemDescription: String = ""
)


@Parcelize
data class MessageFcmMetadata(
    val senderId: String,
    val chatId: String
): Parcelable


@Serializable
data class MessageNotificationRequest(
    val senderId: String,
    val receiverId: String,
    val messageId: String,
    val chatId: String

)

@Serializable
data class CallNotificationRequest(
    val callId: String,
    val channelName: String,
    val callType: String,
    val senderId: String,
    val receiverId: String
)



