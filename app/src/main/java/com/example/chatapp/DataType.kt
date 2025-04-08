package com.example.chatapp

import android.os.Parcelable
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

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

data class CallData(
    val callId: String = "", // call document id
    val callerId: String? = "", // call initiate or caller/ sender id
    val callReceiverId: String? = "", // call receiver
    val callType: String? = "", // type of the call, video or voice
    val channelId: String? = "", // channel name for agora join
    val status: String? = "", // status of the call
    val callStartTime: Timestamp? = Timestamp.now(), // call start time
    val callEndTime: Timestamp? =  Timestamp.now(), // call end time
    val otherUserName: String? = "", // other participant user name
    val otherUserId: String? = "" //other participant of the call

)


@Parcelize
data class CallMetadata(
    val channelName: String,
    val uid: String,
    val callType: String,
    val callerName: String,
    val callReceiverId: String,
    val receiverName: String,
    val isCaller: Boolean
) : Parcelable



