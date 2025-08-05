package com.example.chatapp.call_feature.domain.repository

import com.example.chatapp.call_feature.data.remote_source.model.CallNotificationRequest

interface FcmCallNotificationSenderRepo {

     suspend fun sendCallNotification(request: CallNotificationRequest)
}