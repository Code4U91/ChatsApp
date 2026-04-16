package com.code4u.chatsapp.call_feature.domain.repository

import com.code4u.chatsapp.call_feature.data.remote_source.model.CallNotificationRequest

interface FcmCallNotificationSenderRepo {

     suspend fun sendCallNotification(request: CallNotificationRequest)
}