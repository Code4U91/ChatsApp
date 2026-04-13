package com.code4u.chatsapp.shared.presentation

import com.code4u.chatsapp.call_feature.domain.model.Call

sealed class CallHistoryListState {
    object Loading:  CallHistoryListState()
    data class Success(val calls: List<Call>) :  CallHistoryListState()
}