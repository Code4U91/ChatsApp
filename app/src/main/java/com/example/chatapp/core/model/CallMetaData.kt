package com.example.chatapp.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CallMetadata(
    val channelName: String,
    val uid: String,
    val callType: String,
    val callerName: String,
    val callReceiverId: String,
    val receiverName: String,
    val isCaller: Boolean,
    val receiverPhoto : String,
    val callDocId: String?
) : Parcelable
