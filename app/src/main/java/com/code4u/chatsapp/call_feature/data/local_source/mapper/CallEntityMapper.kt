package com.code4u.chatsapp.call_feature.data.local_source.mapper

import com.code4u.chatsapp.call_feature.data.local_source.entity.CallHistoryEntity
import com.code4u.chatsapp.call_feature.domain.model.Call


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