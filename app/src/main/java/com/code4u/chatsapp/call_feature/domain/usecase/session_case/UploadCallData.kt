package com.code4u.chatsapp.call_feature.domain.usecase.session_case

import com.code4u.chatsapp.call_feature.domain.repository.CallSessionUploaderRepo

class UploadCallData(
    private val callSessionUploaderRepo: CallSessionUploaderRepo
) {
    operator fun invoke(
        callReceiverId: String,
        callType: String,
        channelId: String,
        callStatus: String,
        callerName: String,
        receiverName: String
    ) : String {

       return callSessionUploaderRepo.uploadCallData(
            callReceiverId,
            callType,
            channelId,
            callStatus,
            callerName,
            receiverName
        )

    }
}