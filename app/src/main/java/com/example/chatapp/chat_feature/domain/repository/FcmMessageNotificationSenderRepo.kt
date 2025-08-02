package com.example.chatapp.chat_feature.domain.repository

import com.example.chatapp.core.MessageNotificationRequest

interface FcmMessageNotificationSenderRepo {

    suspend fun sendMessageNotification(request: MessageNotificationRequest)
}