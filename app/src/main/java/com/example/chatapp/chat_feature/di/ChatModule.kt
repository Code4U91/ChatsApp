package com.example.chatapp.chat_feature.di

import android.content.Context
import com.example.chatapp.chat_feature.data.local_source.repositoryIml.LocalChatRepoImpl
import com.example.chatapp.chat_feature.data.remote_source.repositoryImpl.FcmMessageNotificationSenderImpl
import com.example.chatapp.chat_feature.data.remote_source.repositoryImpl.GlobalMessageListenerRepoImpl
import com.example.chatapp.chat_feature.data.remote_source.repositoryImpl.MessagingHandlerRepoImpl
import com.example.chatapp.chat_feature.domain.repository.GlobalMessageListenerRepo
import com.example.chatapp.chat_feature.domain.repository.LocalChatRepo
import com.example.chatapp.chat_feature.domain.repository.MessageHandlerRepo
import com.example.chatapp.chat_feature.domain.message_use_case.CalculateChatId
import com.example.chatapp.chat_feature.domain.message_use_case.ClearAllChatsAndMessageListeners
import com.example.chatapp.chat_feature.domain.message_use_case.DeleteMessages
import com.example.chatapp.chat_feature.domain.message_use_case.GetAllChats
import com.example.chatapp.chat_feature.domain.message_use_case.GetMessage
import com.example.chatapp.chat_feature.domain.message_use_case.MarkMessageAsSeen
import com.example.chatapp.chat_feature.domain.message_use_case.MessageUseCase
import com.example.chatapp.chat_feature.domain.message_use_case.SendMessage
import com.example.chatapp.chat_feature.domain.message_use_case.SyncChats
import com.example.chatapp.chat_feature.domain.repository.FcmMessageNotificationSenderRepo
import com.example.chatapp.core.database.LocalRoomDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModule {

    @Provides
    @Singleton
    fun providesMessageHandlerRepo (
        auth : FirebaseAuth,
        firestoreDb: FirebaseFirestore,
        fcmNotificationSender:  FcmMessageNotificationSenderRepo
    ) : MessageHandlerRepo{

        return MessagingHandlerRepoImpl(
            auth = auth,
            firestoreDb = firestoreDb,
            fcmMessageNotificationSenderRepo = fcmNotificationSender,
        )
    }

    @Provides
    @Singleton
    fun providesGlobalMessageListenerRepo (
        firestoreDb: FirebaseFirestore,
        auth : FirebaseAuth,
        @ApplicationContext context: Context
    ) : GlobalMessageListenerRepo {

        return GlobalMessageListenerRepoImpl(
            firestoreDb = firestoreDb,
            context = context,
            auth = auth
        )
    }

    @Provides
    @Singleton
    fun providesLocalChatRepo(
        db: LocalRoomDatabase
    ) : LocalChatRepo {

        return LocalChatRepoImpl(
            chatDao = db.getChatDao()
        )
    }

    @Provides
    @Singleton
    fun providesFcmMessageNotification(
        client: HttpClient,
        auth : FirebaseAuth
    ) : FcmMessageNotificationSenderRepo{
        return FcmMessageNotificationSenderImpl(
            client = client,
            auth = auth
        )
    }

    // -----
    // PROVIDES USE CASE
    //-----

    @Provides
    @Singleton
    fun providesMessageUseCase(
        localChatRepo : LocalChatRepo,
        messageHandlerRepo: MessageHandlerRepo,
        globalMessageListenerRepo: GlobalMessageListenerRepo
    ) : MessageUseCase {

        return MessageUseCase(
            getMessage = GetMessage(localChatRepo),
            sendMessage = SendMessage(messageHandlerRepo),
            getAllChats = GetAllChats(localChatRepo),
            calculateChatId = CalculateChatId(
                messageHandlerRepo
            ),
            deleteMessages = DeleteMessages(
                messageHandlerRepo = messageHandlerRepo,
                localChatRepo = localChatRepo
            ),
            markMessageAsSeen = MarkMessageAsSeen(messageHandlerRepo),
            syncChats = SyncChats(globalMessageListenerRepo,localChatRepo),
            clearAllChatsAndMessageListeners = ClearAllChatsAndMessageListeners(globalMessageListenerRepo)
        )
    }

}