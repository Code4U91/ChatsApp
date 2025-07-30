package com.example.chatapp.call_feature.data.remote_source.mapper

import com.example.chatapp.call_feature.data.local_source.entity.CallHistoryEntity
import com.example.chatapp.call_feature.data.remote_source.model.CallData


fun CallData.toEntity() : CallHistoryEntity{
    return CallHistoryEntity(
        callId = callId,
        callerId = callerId,
        callReceiverId = callReceiverId,
        callType = callType,
        channelId = channelId,
        status = status,
        callStartTime = callStartTime?.toDate()?.time ?: 0L,
        callEndTime = callEndTime?.toDate()?.time ?: 0L,
        otherUserName = otherUserName,
        otherUserId = otherUserId
    )
}