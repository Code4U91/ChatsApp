package com.example.chatapp.call.presentation.mapper

import com.example.chatapp.call.domain.model.Call
import com.example.chatapp.call.presentation.model.CallUiData

fun Call.toUi() : CallUiData{

    return CallUiData(
        callId = this.callId,
        callerId = this.callerId,
        callReceiverId = this.callReceiverId,
        callType = this.callType,
        channelId = this.channelId,
        status = this.status,
        callStartTime = this.callStartTime,
        callEndTime = this.callEndTime,
        otherUserName = this.otherUserName,
        otherUserId = this.otherUserId
    )

}