package com.example.chatapp.call.data.remote_source.mapper

import com.example.chatapp.call.data.remote_source.model.CallData
import com.example.chatapp.call.domain.model.Call

fun CallData.toDomain(): Call {
 return Call(
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