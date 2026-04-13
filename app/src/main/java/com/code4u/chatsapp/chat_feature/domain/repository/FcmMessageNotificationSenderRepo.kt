package com.code4u.chatsapp.chat_feature.domain.repository

import com.code4u.chatsapp.chat_feature.data.remote_source.model.MessageNotificationRequest

interface FcmMessageNotificationSenderRepo {

    suspend fun sendMessageNotification(request: MessageNotificationRequest)
}