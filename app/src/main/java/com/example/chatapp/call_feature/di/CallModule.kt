package com.example.chatapp.call_feature.di

import android.content.Context
import com.example.chatapp.call_feature.data.local_source.repositoryImpl.LocalCallRepositoryImpl
import com.example.chatapp.call_feature.data.remote_source.repositoryImpl.AgoraSetUpRepoIml
import com.example.chatapp.call_feature.data.remote_source.repositoryImpl.FcmFcmCallNotificationSenderImpl
import com.example.chatapp.call_feature.data.remote_source.repositoryImpl.CallRingtoneManagerIml
import com.example.chatapp.call_feature.data.remote_source.repositoryImpl.CallSessionUpdaterRepoIml
import com.example.chatapp.call_feature.data.remote_source.repositoryImpl.RemoteCallRepoIml
import com.example.chatapp.call_feature.domain.repository.AgoraSetUpRepo
import com.example.chatapp.call_feature.domain.repository.FcmCallNotificationSenderRepo
import com.example.chatapp.call_feature.domain.repository.CallRingtoneRepo
import com.example.chatapp.call_feature.domain.repository.CallSessionUploaderRepo
import com.example.chatapp.call_feature.domain.repository.LocalCallRepository
import com.example.chatapp.call_feature.domain.repository.RemoteCallRepo
import com.example.chatapp.call_feature.domain.usecase.audio_case.CallAudioCase
import com.example.chatapp.call_feature.domain.usecase.audio_case.LocalAudioMuteUseCase
import com.example.chatapp.call_feature.domain.usecase.audio_case.RemoteAudioMuteUseCase
import com.example.chatapp.call_feature.domain.usecase.audio_case.ToggleSpeakerUseCase
import com.example.chatapp.call_feature.domain.usecase.call_case.CallUseCase
import com.example.chatapp.call_feature.domain.usecase.call_case.DeclineCallUseCase
import com.example.chatapp.call_feature.domain.usecase.call_case.EndCallUseCase
import com.example.chatapp.call_feature.domain.usecase.call_case.StartCallUseCase
import com.example.chatapp.call_feature.domain.usecase.call_history_case.CallHistoryUseCase
import com.example.chatapp.call_feature.domain.usecase.call_history_case.ClearCallHistoryListener
import com.example.chatapp.call_feature.domain.usecase.call_history_case.GetCallHistoryUseCase
import com.example.chatapp.call_feature.domain.usecase.call_history_case.SyncCallHistoryUseCase
import com.example.chatapp.call_feature.domain.usecase.call_invite_fcm.SendCallInviteNotification
import com.example.chatapp.call_feature.domain.usecase.call_video_case.CallVideoCase
import com.example.chatapp.call_feature.domain.usecase.call_video_case.EnableVideoPreviewUseCase
import com.example.chatapp.call_feature.domain.usecase.call_video_case.SetUpLocalVideoUseCase
import com.example.chatapp.call_feature.domain.usecase.call_video_case.SetUpRemoteVideoUseCase
import com.example.chatapp.call_feature.domain.usecase.call_video_case.SwitchCameraUseCase
import com.example.chatapp.call_feature.domain.usecase.ringtone_case.PlayIncomingRingtone
import com.example.chatapp.call_feature.domain.usecase.ringtone_case.PlayOutgoingRingtone
import com.example.chatapp.call_feature.domain.usecase.ringtone_case.RingtoneUseCase
import com.example.chatapp.call_feature.domain.usecase.ringtone_case.StopAllRingtone
import com.example.chatapp.call_feature.domain.usecase.session_case.CallSessionUploadCase
import com.example.chatapp.call_feature.domain.usecase.session_case.CheckCurrentCallStatus
import com.example.chatapp.call_feature.domain.usecase.session_case.UpdateCallStatus
import com.example.chatapp.call_feature.domain.usecase.session_case.UploadCallData
import com.example.chatapp.call_feature.domain.usecase.session_case.UploadDataOnCallEnd
import com.example.chatapp.core.local_database.LocalRoomDatabase
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
object CallModule {

    @Provides
    @Singleton
    fun providesAgoraRepo(
        @ApplicationContext context: Context
    ): AgoraSetUpRepo {
        return AgoraSetUpRepoIml(context)
    }

    @Provides
    @Singleton
    fun provideCallRepository(db: LocalRoomDatabase): LocalCallRepository {

        return LocalCallRepositoryImpl(db.callDao())
    }

    @Provides
    @Singleton
    fun provideCallSessionRepo(
        auth: FirebaseAuth,
        remoteDb: FirebaseFirestore
    ): CallSessionUploaderRepo {
        return CallSessionUpdaterRepoIml(auth, remoteDb)
    }

    @Provides
    @Singleton
    fun providesRemoteCallRepo(auth: FirebaseAuth, remoteDb: FirebaseFirestore): RemoteCallRepo {

        return RemoteCallRepoIml(auth, remoteDb)
    }

    @Provides
    @Singleton
    fun providesCallRingtoneRepo(@ApplicationContext context: Context): CallRingtoneRepo {
        return CallRingtoneManagerIml(context)
    }

    @Provides
    @Singleton
    fun providesFcmCallNotificationInvite(
        client: HttpClient,
        auth: FirebaseAuth

    ): FcmCallNotificationSenderRepo {

        return FcmFcmCallNotificationSenderImpl(
            client = client,
            auth = auth
        )
    }


    // ----- USE CASE ------

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
    fun providesAudioUseCase(agoraSetUpRepo: AgoraSetUpRepo): CallAudioCase {
        return CallAudioCase(

            localAudioMuteUseCase = LocalAudioMuteUseCase(agoraSetUpRepo),
            remoteAudioMuteUseCase = RemoteAudioMuteUseCase(agoraSetUpRepo),
            toggleSpeakerUseCase = ToggleSpeakerUseCase(agoraSetUpRepo)
        )
    }

    @Provides
    @Singleton
    fun providesVideoUseCase(agoraSetUpRepo: AgoraSetUpRepo): CallVideoCase {

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
        localCallRepository: LocalCallRepository,
        remoteCallRepo: RemoteCallRepo
    ): CallHistoryUseCase {

        return CallHistoryUseCase(
            getCallHistoryUseCase = GetCallHistoryUseCase(localCallRepository),
            syncCallHistoryUseCase = SyncCallHistoryUseCase(remoteCallRepo, localCallRepository),
            clearCallHistoryListener = ClearCallHistoryListener(remoteCallRepo)
        )

    }

    @Provides
    @Singleton
    fun providesRingtoneUseCase(
        callRingtoneRepo: CallRingtoneRepo
    ): RingtoneUseCase {

        return RingtoneUseCase(

            playIncomingRingtone = PlayIncomingRingtone(callRingtoneRepo),
            playOutgoingRingtone = PlayOutgoingRingtone(callRingtoneRepo),
            stopAllRingtone = StopAllRingtone(callRingtoneRepo)
        )

    }

    @Provides
    @Singleton
    fun providesCallSessionUploadUseCase(
        callSessionUploaderRepo: CallSessionUploaderRepo
    ): CallSessionUploadCase {

        return CallSessionUploadCase(
            uploadCallData = UploadCallData(callSessionUploaderRepo),
            updateCallStatus = UpdateCallStatus(callSessionUploaderRepo),
            checkCurrentCallStatus = CheckCurrentCallStatus(callSessionUploaderRepo),
            uploadDataOnCallEnd = UploadDataOnCallEnd(callSessionUploaderRepo)
        )

    }

    @Provides
    @Singleton
    fun providesCallInviteUseCase(
        fcmCallNotificationSenderRepo: FcmCallNotificationSenderRepo
    ) : SendCallInviteNotification {
        return SendCallInviteNotification(fcmCallNotificationSenderRepo)
    }
}