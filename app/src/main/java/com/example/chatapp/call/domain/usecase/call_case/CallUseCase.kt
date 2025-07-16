package com.example.chatapp.call.domain.usecase.call_case 

data class CallUseCase (
    val startCallUseCase: StartCallUseCase,
    val endCallUseCase: EndCallUseCase,
    val declineCallUseCase: DeclineCallUseCase
)