package com.example.chatapp.call.di

import android.content.Context
import com.example.chatapp.call.data.remote_source.repositoryImpl.AgoraSetUpRepoIml
import com.example.chatapp.call.data.remote_source.repositoryImpl.CallRepositoryImpl
import com.example.chatapp.call.data.remote_source.repositoryImpl.CallSessionUpdaterRepoIml
import com.example.chatapp.call.data.remote_source.repositoryImpl.RemoteCallRepoIml
import com.example.chatapp.call.domain.repository.AgoraSetUpRepo
import com.example.chatapp.call.domain.repository.CallRepository
import com.example.chatapp.call.domain.repository.CallSessionUploaderRepo
import com.example.chatapp.call.domain.repository.RemoteCallRepo
import com.example.chatapp.call.domain.usecase.audio_case.CallAudioCase
import com.example.chatapp.call.domain.usecase.audio_case.LocalAudioMuteUseCase
import com.example.chatapp.call.domain.usecase.audio_case.RemoteAudioMuteUseCase
import com.example.chatapp.call.domain.usecase.audio_case.ToggleSpeakerUseCase
import com.example.chatapp.call.domain.usecase.call_case.CallUseCase
import com.example.chatapp.call.domain.usecase.call_case.DeclineCallUseCase
import com.example.chatapp.call.domain.usecase.call_case.EndCallUseCase
import com.example.chatapp.call.domain.usecase.call_case.StartCallUseCase
import com.example.chatapp.call.domain.usecase.call_history_case.CallHistoryUseCase
import com.example.chatapp.call.domain.usecase.call_history_case.GetCallHistoryUseCase
import com.example.chatapp.call.domain.usecase.call_history_case.InsertCallHistoryCase
import com.example.chatapp.call.domain.usecase.call_video_case.CallVideoCase
import com.example.chatapp.call.domain.usecase.call_video_case.EnableVideoPreviewUseCase
import com.example.chatapp.call.domain.usecase.call_video_case.SetUpLocalVideoUseCase
import com.example.chatapp.call.domain.usecase.call_video_case.SetUpRemoteVideoUseCase
import com.example.chatapp.call.domain.usecase.call_video_case.SwitchCameraUseCase
import com.example.chatapp.core.local_database.LocalRoomDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CallModule {

    @Provides
    @Singleton
    fun providesAgoraRepo(
        @ApplicationContext context: Context
    ) : AgoraSetUpRepo {
        return AgoraSetUpRepoIml(context)
    }

    @Provides
    @Singleton
    fun provideCallRepository(db: LocalRoomDatabase): CallRepository {

        return CallRepositoryImpl(db.callDao)
    }

    @Provides
    @Singleton
    fun provideCallSessionRepo(auth: FirebaseAuth, remoteDb : FirebaseFirestore) : CallSessionUploaderRepo {
        return CallSessionUpdaterRepoIml(auth, remoteDb)
    }

    @Provides
    @Singleton
    fun providesRemoteCallRepo(auth: FirebaseAuth, remoteDb : FirebaseFirestore) : RemoteCallRepo {

        return RemoteCallRepoIml(auth, remoteDb)
    }

    @Provides
    @Singleton
    fun providesCallUseCase(
        auth: FirebaseAuth,
        agora: AgoraSetUpRepo,
        callSessionUploaderRepo: CallSessionUploaderRepo
    ): CallUseCase {
        return CallUseCase(
            startCallUseCase = StartCallUseCase(auth),

            endCallUseCase = EndCallUseCase(agora),

            declineCallUseCase = DeclineCallUseCase(
                agoraSetUpRepo = agora,
                callSessionUploaderRepo = callSessionUploaderRepo
            )
        )
    }

    @Provides
    @Singleton
    fun providesAudioUseCase(agoraSetUpRepo: AgoraSetUpRepo) : CallAudioCase {
        return CallAudioCase(

            localAudioMuteUseCase = LocalAudioMuteUseCase(agoraSetUpRepo),
            remoteAudioMuteUseCase = RemoteAudioMuteUseCase(agoraSetUpRepo),
            toggleSpeakerUseCase = ToggleSpeakerUseCase(agoraSetUpRepo)
        )
    }

    @Provides
    @Singleton
    fun providesVideoUseCase(agoraSetUpRepo: AgoraSetUpRepo) : CallVideoCase{

        return CallVideoCase(
            enableVideoPreviewUseCase = EnableVideoPreviewUseCase(agoraSetUpRepo),
            setUpRemoteVideoUseCase = SetUpRemoteVideoUseCase(agoraSetUpRepo),
            setUpLocalVideoUseCase = SetUpLocalVideoUseCase(agoraSetUpRepo),
            switchCameraUseCase = SwitchCameraUseCase(agoraSetUpRepo)
        )
    }

    @Provides
    @Singleton
    fun providesCallHistoryUseCase(
        callRepository: CallRepository,
        remoteCallRepo: RemoteCallRepo
    ) : CallHistoryUseCase {

        return CallHistoryUseCase(
            getCallHistoryUseCase = GetCallHistoryUseCase(callRepository,remoteCallRepo),
            insertCallHistoryCase = InsertCallHistoryCase(callRepository)
        )

    }
}