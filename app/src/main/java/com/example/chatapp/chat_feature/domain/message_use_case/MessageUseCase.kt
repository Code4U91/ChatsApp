package com.example.chatapp.chat_feature.domain.message_use_case

data class MessageUseCase(
    val getMessage: GetMessage,
    val sendMessage: SendMessage,
    val getAllChats: GetAllChats,
    val calculateChatId: CalculateChatId,
    val deleteMessages: DeleteMessages,
    val markMessageAsSeen: MarkMessageAsSeen,
    val syncChats: SyncChats,
    val clearAllChatsAndMessageListeners: ClearAllChatsAndMessageListeners,
    val loadOldMessageOnce: LoadOldMessageOnce
)
