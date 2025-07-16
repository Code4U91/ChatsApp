package com.example.chatapp.call.domain.usecase.call_history_case

data class CallHistoryUseCase(
    val getCallHistoryUseCase: GetCallHistoryUseCase,
    val insertCallHistoryCase: InsertCallHistoryCase
)
