package com.code4u.chatsapp.call_feature.domain.usecase.call_history_case

data class CallHistoryUseCase(
    val getCallHistoryUseCase: GetCallHistoryUseCase,
    val syncCallHistoryUseCase: SyncCallHistoryUseCase,
    val clearCallHistoryListener: ClearCallHistoryListener
)
