package com.example.chatapp

import kotlinx.coroutines.flow.MutableSharedFlow

const val USERS_COLLECTION = "users"

const val USERS_REF = "users"

const val CHATS_COLLECTION = "chats"

const val FRIEND_COLLECTION = "friendList"

const val MESSAGE_COLLECTION = "messages"

const val MAIN_GRAPH_ROUTE = "main_graph"

const val AUTH_GRAPH_ROUTE = "auth_graph"

const val CALL_HISTORY = "calls"

const val CALL_CHANNEL_NOTIFICATION_NAME_ID = "call_channel"

const val CALL_FCM_NOTIFICATION_CHANNEL_STRING = "call_fcm_channel"
const val MESSAGE_FCM_CHANNEL_STRING = "message_fcm_channel"

const val CALL_FCM_NOTIFICATION_ID = 101
const val CALL_SERVICE_ACTIVE_NOTIFICATION_ID = 102
const val MESSAGE_FCM_NOTIFICATION_ID = 201

const val AGORA_ID = BuildConfig.AGORA_APP_ID

object CallEventHandler {
    val incomingCall = MutableSharedFlow<CallMetadata>(replay = 0)
}
