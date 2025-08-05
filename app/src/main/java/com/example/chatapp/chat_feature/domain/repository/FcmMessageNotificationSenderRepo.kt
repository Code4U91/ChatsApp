package com.example.chatapp.chat_feature.domain.repository

import com.example.chatapp.chat_feature.data.remote_source.model.MessageNotificationRequest

interface FcmMessageNotificationSenderRepo {

    suspend fun sendMessageNotification(request: MessageNotificationRequest)
}