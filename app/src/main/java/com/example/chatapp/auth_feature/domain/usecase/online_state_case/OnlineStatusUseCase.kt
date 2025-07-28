package com.example.chatapp.auth_feature.domain.usecase.online_state_case

data class OnlineStatusUseCase(
    val setOnlineStatus: SetOnlineStatus,
    val setActiveChatUseCase: SetActiveChatUseCase,
    val listenForOnlineStatus: ListenForOnlineStatus
)