package com.example.chatapp.chat_feature.di

import android.content.Context
import com.example.chatapp.api.FcmNotificationSender
import com.example.chatapp.auth_feature.domain.usecase.auth_case.AuthUseCase
import com.example.chatapp.chat_feature.data.local_source.repositoryIml.LocalChatRepoImpl
import com.example.chatapp.chat_feature.data.remote_source.repositoryImpl.GlobalMessageListenerRepoImpl
import com.example.chatapp.chat_feature.data.remote_source.repositoryImpl.MessagingHandlerRepoImpl
import com.example.chatapp.chat_feature.domain.repository.GlobalMessageListenerRepo
import com.example.chatapp.chat_feature.domain.repository.LocalChatRepo
import com.example.chatapp.chat_feature.domain.repository.MessageHandlerRepo
import com.example.chatapp.chat_feature.domain.use_case.message_use_case.CalculateChatId
import com.example.chatapp.chat_feature.domain.use_case.message_use_case.DeleteMessages
import com.example.chatapp.chat_feature.domain.use_case.message_use_case.GetAllChats
import com.example.chatapp.chat_feature.domain.use_case.message_use_case.GetMessage
import com.example.chatapp.chat_feature.domain.use_case.message_use_case.MessageUseCase
import com.example.chatapp.chat_feature.domain.use_case.message_use_case.SendMessage
import com.example.chatapp.chat_feature.domain.use_case.sync_use_case.ChatsSyncAndUnSyncUseCase
import com.example.chatapp.chat_feature.domain.use_case.sync_use_case.SyncChats
import com.example.chatapp.core.local_database.LocalRoomDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModule {

    @Provides
    @Singleton
    fun providesMessageHandlerRepo (
        auth : FirebaseAuth,
        firestoreDb: FirebaseFirestore,
        firebaseMessaging: FirebaseMessaging,
        fcmNotificationSender: FcmNotificationSender,
        @ApplicationContext context: Context
    ) : MessageHandlerRepo{

        return MessagingHandlerRepoImpl(
            auth = auth,
            firestoreDb = firestoreDb,
            firebaseMessaging = firebaseMessaging,
            fcmNotificationSender = fcmNotificationSender,
            context =  context
        )
    }

    @Provides
    @Singleton
    fun providesGlobalMessageListenerRepo (
        auth : FirebaseAuth,
        firestoreDb: FirebaseFirestore,
        @ApplicationContext context: Context
    ) : GlobalMessageListenerRepo {

        return GlobalMessageListenerRepoImpl(
            auth = auth,
            firestoreDb = firestoreDb,
            context = context
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

    // -----
    // PROVIDES USE CASE
    //-----

    @Provides
    @Singleton
    fun providesMessageUseCase(
        localChatRepo : LocalChatRepo,
        messageHandlerRepo: MessageHandlerRepo,
        authUseCase: AuthUseCase
    ) : MessageUseCase {

        return MessageUseCase(
            getMessage = GetMessage(localChatRepo),
            sendMessage = SendMessage(messageHandlerRepo),
            getAllChats = GetAllChats(localChatRepo),
            calculateChatId = CalculateChatId(
                messageHandlerRepo,
                authUseCase = authUseCase
            ),
            deleteMessages = DeleteMessages(
                messageHandlerRepo = messageHandlerRepo,
                localChatRepo = localChatRepo
            )
        )
    }

    @Provides
    @Singleton
    fun providesChatSyncUseCase(
        globalMessageListenerRepo: GlobalMessageListenerRepo,
        localChatRepo: LocalChatRepo,
        authUseCase: AuthUseCase
    ) : ChatsSyncAndUnSyncUseCase {
        return ChatsSyncAndUnSyncUseCase(
            syncChats = SyncChats(
                globalMessageListenerRepo = globalMessageListenerRepo,
                localChatRepo = localChatRepo,
                authUseCase =  authUseCase
            )
        )
    }
}