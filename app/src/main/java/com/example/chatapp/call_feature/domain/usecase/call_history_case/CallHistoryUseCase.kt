package com.example.chatapp.call_feature.domain.usecase.call_history_case

data class CallHistoryUseCase(
    val getCallHistoryUseCase: GetCallHistoryUseCase,
    val syncCallHistoryUseCase: SyncCallHistoryUseCase
)
