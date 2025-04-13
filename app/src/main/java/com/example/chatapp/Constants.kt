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

const val CALL_CHANNEL_NOTIFICATION_ID = "call_channel"

const val ACTION_ANSWER_CALL = "answer"
const val ACTION_DECLINE_CALL = "decline"

const val AGORA_ID = BuildConfig.AGORA_APP_ID

object CallEventHandler {
    val incomingCall = MutableSharedFlow<CallMetadata>(replay = 0)
}
