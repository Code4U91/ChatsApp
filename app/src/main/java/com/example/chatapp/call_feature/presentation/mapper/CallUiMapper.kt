package com.example.chatapp.call_feature.presentation.mapper

import com.example.chatapp.call_feature.domain.model.Call
import com.example.chatapp.call_feature.presentation.model.CallUiData

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