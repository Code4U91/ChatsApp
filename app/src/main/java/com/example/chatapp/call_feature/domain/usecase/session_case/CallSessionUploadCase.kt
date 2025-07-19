package com.example.chatapp.call_feature.domain.usecase.session_case

data class CallSessionUploadCase(
    val checkCurrentCallStatus: CheckCurrentCallStatus,
    val updateCallStatus: UpdateCallStatus,
    val uploadCallData: UploadCallData,
    val uploadDataOnCallEnd: UploadDataOnCallEnd
)
