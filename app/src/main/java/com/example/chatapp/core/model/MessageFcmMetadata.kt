package com.example.chatapp.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MessageFcmMetadata(
    val senderId: String,
    val chatId: String
): Parcelable
