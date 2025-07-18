package com.example.chatapp.call.data.local_source.mapper

import com.example.chatapp.call.data.local_source.entity.CallHistoryEntity
import com.example.chatapp.call.domain.model.Call


fun CallHistoryEntity.toDomain(): Call = Call(
    callId = callId,
    callerId = callerId,
    callReceiverId = callReceiverId,
    callType = callType,
    channelId = channelId,
    status = status,
    callStartTime = callStartTime,
    callEndTime = callEndTime,
    otherUserName = otherUserName,
    otherUserId = otherUserId
)

fun Call.toEntity(): CallHistoryEntity = CallHistoryEntity(
    callId = callId,
    callerId = callerId,
    callReceiverId = callReceiverId,
    callType = callType,
    channelId = channelId,
    status = status,
    callStartTime = callStartTime,
    callEndTime = callEndTime,
    otherUserName = otherUserName,
    otherUserId = otherUserId
)