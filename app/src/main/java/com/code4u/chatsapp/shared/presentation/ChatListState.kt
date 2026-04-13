package com.code4u.chatsapp.shared.presentation

import com.code4u.chatsapp.chat_feature.domain.model.Chat

sealed class ChatListState {
    object Loading: ChatListState()
    data class Success(val chats: List<Chat>) : ChatListState()
}