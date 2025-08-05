package com.example.chatapp.call_feature.domain.usecase.call_invite_fcm

import com.example.chatapp.call_feature.data.remote_source.model.CallNotificationRequest
import com.example.chatapp.call_feature.domain.repository.FcmCallNotificationSenderRepo

class SendCallInviteNotification (
    private val fcmCallNotificationSenderRepo: FcmCallNotificationSenderRepo
) {
    suspend operator fun invoke(request: CallNotificationRequest){

        fcmCallNotificationSenderRepo.sendCallNotification(request)
    }
}