package com.example.chatapp.chat_feature.domain.use_case.message_use_case

data class MessageUseCase(
    val getMessage: GetMessage,
    val sendMessage: SendMessage,
    val getAllChats: GetAllChats,
    val calculateChatId: CalculateChatId,
    val deleteMessages: DeleteMessages
)
