package com.code4u.chatsapp.call_feature.domain.usecase.call_invite_fcm

import com.code4u.chatsapp.call_feature.data.remote_source.model.CallNotificationRequest
import com.code4u.chatsapp.call_feature.domain.repository.FcmCallNotificationSenderRepo

class SendCallInviteNotification (
    private val fcmCallNotificationSenderRepo: FcmCallNotificationSenderRepo
) {
    suspend operator fun invoke(request: CallNotificationRequest){

        fcmCallNotificationSenderRepo.sendCallNotification(request)
    }
}