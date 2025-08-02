package com.example.chatapp.call_feature.domain.repository

import com.example.chatapp.core.CallNotificationRequest

interface FcmCallNotificationSenderRepo {

     suspend fun sendCallNotification(request: CallNotificationRequest)
}