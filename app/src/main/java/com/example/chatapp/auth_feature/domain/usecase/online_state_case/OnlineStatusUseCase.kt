package com.example.chatapp.auth_feature.domain.usecase.online_state_case

import com.example.chatapp.auth_feature.domain.usecase.online_state_case.ListenForOnlineStatus
import com.example.chatapp.auth_feature.domain.usecase.online_state_case.SetActiveChatUseCase
import com.example.chatapp.auth_feature.domain.usecase.online_state_case.SetOnlineStatus

data class OnlineStatusUseCase(
    val setOnlineStatus: SetOnlineStatus,
    val setActiveChatUseCase: SetActiveChatUseCase,
    val listenForOnlineStatus: ListenForOnlineStatus
)