package com.example.chatapp.auth_feature.di

import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import com.example.chatapp.auth_feature.data.repositoryIml.AuthRepositoryIml
import com.example.chatapp.auth_feature.data.repositoryIml.OnlineStatusRepoIml
import com.example.chatapp.auth_feature.domain.repository.AuthRepository
import com.example.chatapp.auth_feature.domain.repository.OnlineStatusRepo
import com.example.chatapp.auth_feature.domain.usecase.auth_case.AuthUseCase
import com.example.chatapp.auth_feature.domain.usecase.auth_case.ChangeEmailUseCase
import com.example.chatapp.auth_feature.domain.usecase.auth_case.GetCurrentUser
import com.example.chatapp.auth_feature.domain.usecase.auth_case.ResetPasswordUseCase
import com.example.chatapp.auth_feature.domain.usecase.auth_case.SignInUseCase
import com.example.chatapp.auth_feature.domain.usecase.auth_case.SignOutUseCase
import com.example.chatapp.auth_feature.domain.usecase.auth_case.SignUpUseCase
import com.example.chatapp.auth_feature.domain.usecase.online_state_case.ListenForOnlineStatus
import com.example.chatapp.auth_feature.domain.usecase.online_state_case.OnlineStatusUseCase
import com.example.chatapp.auth_feature.domain.usecase.online_state_case.SetActiveChatUseCase
import com.example.chatapp.auth_feature.domain.usecase.online_state_case.SetOnlineStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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
object AuthModule {

    @Provides
    @Singleton
    fun providesFirebase(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun providesSignClient(context: Application): CredentialManager {
        return CredentialManager.create(context)
    }

    @Provides
    @Singleton
    fun firestoreDb(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun realTimeDb(): FirebaseDatabase {
        return FirebaseDatabase.getInstance()
    }

    @Provides
    @Singleton
    fun firebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

    @Provides
    @Singleton
    fun providesAuthRepo(
        auth: FirebaseAuth,
        credentialManager: CredentialManager,
        db: FirebaseFirestore,
        realTimeDb: FirebaseDatabase,
        firebaseMessaging: FirebaseMessaging,
        @ApplicationContext context: Context

    ): AuthRepository {
        return AuthRepositoryIml(
            auth = auth,
            credentialManager = credentialManager,
            firestoreDb = db,
            realTimeDb = realTimeDb,
            firebaseMessaging = firebaseMessaging,
            context = context
        )
    }

    @Provides
    @Singleton
    fun providesOnlineStatusRepo(
        auth : FirebaseAuth,
        realTimeDb : FirebaseDatabase

    ) : OnlineStatusRepo {
        return OnlineStatusRepoIml(
            auth = auth,
            realTimeDb = realTimeDb
        )
    }

    //----
    // AUTH USE CASE PROVIDER
    //----


    @Provides
    @Singleton
    fun providesAuthUseCase(
        authRepository: AuthRepository
    ): AuthUseCase {
        return AuthUseCase(
            signInUseCase = SignInUseCase(authRepository),
            signOutUseCase = SignOutUseCase(authRepository),
            changeEmailUseCase = ChangeEmailUseCase(authRepository),
            resetPasswordUseCase = ResetPasswordUseCase(authRepository),
            getCurrentUser = GetCurrentUser(authRepository),
            signUpUseCase = SignUpUseCase(authRepository)
        )
    }

    @Provides
    @Singleton
    fun providesOnlineStateCase(
        setOnlineStatusRepo: OnlineStatusRepo
    ) : OnlineStatusUseCase{
        return OnlineStatusUseCase(
            setOnlineStatus = SetOnlineStatus(setOnlineStatusRepo),
            setActiveChatUseCase = SetActiveChatUseCase(setOnlineStatusRepo),
            listenForOnlineStatus = ListenForOnlineStatus(setOnlineStatusRepo)
        )
    }

}