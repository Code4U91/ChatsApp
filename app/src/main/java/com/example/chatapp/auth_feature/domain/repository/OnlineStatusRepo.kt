package com.example.chatapp.auth_feature.domain.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

interface OnlineStatusRepo {

    fun activeChatUpdate(chatId: String)

    fun setOnlineStatusWithDisconnect(status: Boolean, chatId: String = "")

    fun listenForOnlineStatus(
        userId: String,
        onStatusChanged: (Long) -> Unit
    ): Pair<DatabaseReference, ValueEventListener>

}