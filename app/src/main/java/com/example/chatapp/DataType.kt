package com.example.chatapp

import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.Timestamp

data class UserData(
    val uid: String? = "",
    val name: String? = "",
    val photoUrl: String? = "",
    val email: String? = "",
    val about: String? = "",
    val fcmTokens: List<String> = emptyList()
)

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

data class Message(
    val messageContent: String? = "",
    val messageId: String = "",
    val receiverId: String? = "",
    val senderId: String? = "",
    val status: String? = "",  // sending -> sent -> delivered -> seen
    val timeStamp: Timestamp? = Timestamp.now(),
)

data class FriendData(
    val name: String? = "",
    val photoUrl: String? = "",
    val about: String? = "",
    val uid: String? = "",
    val fcmTokens: List<String> = emptyList()
)

data class ChatItemData(
    val chatId: String = "",
    val otherUserId: String? = "",
    val lastMessage: String? = "",
    val lastMessageTimeStamp: Timestamp? = Timestamp.now(),
    val otherUserName: String? = ""
)

data class FriendListData(
    val friendId: String = "",
    val friendName: String = ""
)
