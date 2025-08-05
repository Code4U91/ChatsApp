package com.example.chatapp.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.chatapp.R
import com.example.chatapp.call_feature.data.remote_source.model.CallNotificationRequest
import com.example.chatapp.call_feature.domain.repository.AgoraSetUpRepo
import com.example.chatapp.call_feature.domain.usecase.call_invite_fcm.SendCallInviteNotification
import com.example.chatapp.call_feature.domain.usecase.ringtone_case.RingtoneUseCase
import com.example.chatapp.call_feature.domain.usecase.session_case.CallSessionUploadCase
import com.example.chatapp.call_feature.presentation.call_screen.activity.CallActivity
import com.example.chatapp.core.util.AGORA_ID
import com.example.chatapp.core.util.CALL_CHANNEL_NOTIFICATION_NAME_ID
import com.example.chatapp.core.util.CALL_INTENT
import com.example.chatapp.core.util.CALL_SERVICE_ACTIVE_NOTIFICATION_ID
import com.example.chatapp.core.util.INCOMING_CALL_FCM_NOTIFICATION_ID
import com.example.chatapp.core.model.CallMetadata
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AgoraCallService : LifecycleService() {


    @Inject
    lateinit var agoraRepo: AgoraSetUpRepo

    @Inject
    lateinit var callSessionUploadCase: CallSessionUploadCase

    @Inject
    lateinit var ringtoneUseCase: RingtoneUseCase

    @Inject
    lateinit var  sendCallInviteNotification: SendCallInviteNotification



    private lateinit var callMetadata: CallMetadata

    private var callId: String? = null   // callDocId
    private var isRemoteUserLeft: Boolean = false

    private var isRemoteUserJoined: Int? = null

    private var serviceJob: Job? = null

    private var isCallDeclined: Boolean = false

    private var hasServiceStarted: Boolean = false

    private val listenerRegistration = mutableListOf<ListenerRegistration>()

    private val _callDuration = MutableStateFlow(0L) // in seconds

    private var callTimerJob: Job? = null

    //private val callEvent = agoraRepo.callEvent

    override fun onCreate() {
        super.onCreate()

        agoraRepo.initializeAgora(AGORA_ID)

    }


    // currently runs each time start foreground is called
    @SuppressLint("NewApi")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // checking so that if service is started again while its already running the functions inside onStartCommand
        // should not run again, fixes issues like notification spamming
        if (!hasServiceStarted) {

            val metaData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.getParcelableExtra("call_metadata", CallMetadata::class.java)
            } else {
                // Fallback for older version
                @Suppress("DEPRECATION")
                intent?.getParcelableExtra("call_metadata")
            }


            metaData?.let {

                callMetadata = it

                val inOrOut = if (it.isCaller) "Outgoing" else "Incoming"
                val callNotification = it.channelName.hashCode()

                // support for lower version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        callNotification + CALL_SERVICE_ACTIVE_NOTIFICATION_ID,
                        buildNotification(
                            title = if (it.isCaller) it.receiverName else it.callerName,
                            contextText = "$inOrOut ${it.callType} call",
                            callNotification),
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or
                                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                    )
                } else {

                    startForeground(
                        callNotification + CALL_SERVICE_ACTIVE_NOTIFICATION_ID,
                        buildNotification(
                            title = if (it.isCaller) it.receiverName else it.callerName,
                            contextText = "$inOrOut ${it.callType} call",
                            callNotification)
                    )
                }


                if (it.isCaller) // directly join the call if the user is a caller
                {
                    val speaker = it.callType == "video"
                     ringtoneUseCase.playOutgoingRingtone(speaker)
                }
                lifecycleScope.launch {
                    agoraRepo.joinChannel(null, it.channelName, it.callType, it.uid)
                }
                // incoming calls are received by fcm push notification it runs incoming ringtone there using callRingtoneManager


                startFlowCollectors(callNotification) // callDocId in case needed by the receiver, its firebase doc id where the current
                // active call data is store, may require to update the data directly without querying

            }

        }

        hasServiceStarted = true

        return START_STICKY
    }

    // handles all the coroutine background tasks

    private fun startFlowCollectors(callNotification: Int) {

        serviceJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // responsible for collecting join status, here join refers to the user joining the channel
                launch {
                    agoraRepo.isJoined.collect { isJoined ->

                        Log.i("AGORA_CALL_SERVICE", "isJoined : $isJoined")

                        if (isJoined) {

                            // clear previous listeners if exists, which no longer needed
                            // like listenerForDeclineByCaller which runs while the receiver have not accepted the call
                            // as soon as call is accepted we reach inside this joined block and call becomes active and no longer needs
                            // to listen whether the call got cancelled by caller
                            clearListeners()

                            // only caller creates a document/session
                            // upload data if the user have joined the channel and the user is an caller
                            if (callMetadata.isCaller) {

                                callId = callSessionUploadCase.uploadCallData(
                                    callReceiverId = callMetadata.callReceiverId,
                                    callType = callMetadata.callType,
                                    channelId = callMetadata.channelName,
                                    callStatus = "ringing",
                                    callerName = callMetadata.callerName,
                                    receiverName = callMetadata.receiverName
                                )

                                callId?.let {

                                    // sending call invitation after creating call session/ call document
                                      sendCallInviteNotification(
                                          CallNotificationRequest(
                                              callId = it,
                                              channelName = callMetadata.channelName,
                                              callType = callMetadata.callType,
                                              senderId = callMetadata.uid,
                                              receiverId = callMetadata.callReceiverId
                                          )
                                    )

                                    // when the receiver declines the call
                                    val listenerForCallDecline =
                                        callSessionUploadCase.checkCurrentCallStatus(callId = it)
                                        {
                                            isCallDeclined = true
                                            agoraRepo.declineIncomingCall(true) // declined by receiver
                                            stopSelf() //  if the auto re- calling issue occurs after ending the call them check for this stopSelf

                                        }

                                    listenerRegistration.add(listenerForCallDecline)
                                }
                            }


                        }
                        //else {
//
//                            // check if the call gets canceled before the receiver picks it
//                            // happens if the user calls and before receiver picks the call caller cuts it
//                            // in that case close call screen on receiver side
//                            // receiver can't join if call isn't picked so join is false
//                            // checked by the receiver, i.e isCaller false
//
//                            callDocId?.let {
//                                val listenerForDeclineByCaller =
//                                    callSessionUpdaterRepoIml.checkAndUpdateCurrentCall(callId = it)
//                                    {
//
//                                        agoraRepo.declineIncomingCall(true) // helper flag to update the ui, listened by callViewmodel
//
//                                    }
//
//                                listenerRegistration.add(listenerForDeclineByCaller)
//                            }
//
//
//                        }


                    }
                }

                // check if the call became active, i.e other user has joined
                launch {
                    agoraRepo.remoteUserJoined.collect { remoteUser ->

                        Log.i("AGORA_CALL_SERVICE", "RemoteUserJoined: ${remoteUser.toString()}")
                        if (remoteUser != null) {

                            ringtoneUseCase.stopAllRingtone()

                            startCallTimer()
                            isRemoteUserJoined = remoteUser

                            val notificationManager =
                                getSystemService(NOTIFICATION_SERVICE) as NotificationManager



                            notificationManager.notify(
                               callNotification + CALL_SERVICE_ACTIVE_NOTIFICATION_ID,
                                buildNotification(
                                    title = if (callMetadata.isCaller) callMetadata.receiverName else callMetadata.callerName,
                                    contextText = "Ongoing call",
                                    callNotificationId = callNotification)
                            )


                            callId?.let { callDocId ->
                                callSessionUploadCase.updateCallStatus("ongoing", callDocId)
                            }

                        }

                    }
                }

                // check if the other user have left the call if yes, stop the service
                launch {
                    agoraRepo.remoteUserLeft.collect { remoteUserLeft ->

                        isRemoteUserLeft = remoteUserLeft
                        if (remoteUserLeft) {
                            stopCallTimer()
                            stopSelf()
                            Log.i("AGORA_CALL_SERVICE", "Executed StopSelf")
                        }
                        Log.i("AGORA_CALL_SERVICE", "RemoteUserLeft: $remoteUserLeft")
                    }
                }

            }
        }

    }

    private fun clearListeners() {
        listenerRegistration.forEach { it.remove() }
        listenerRegistration.clear()
    }

    override fun onDestroy() {
        super.onDestroy()

        ringtoneUseCase.stopAllRingtone()


        clearListeners()
        serviceJob?.cancel()

        Log.i("AGORA_CALL_SERVICE", "OnDestroy called")

        // do not update the document data if the user is caller or the user's call got declined by receiver
        if (callMetadata.isCaller && !isCallDeclined) {
            callId?.let { callDocId ->

                val status = if (isRemoteUserJoined != null) "ended" else "missed"

                callSessionUploadCase.uploadDataOnCallEnd(status, callDocId)

            }
        }

        // reset the state
        isCallDeclined = false
        hasServiceStarted = false

        agoraRepo.destroy()

    }


    private fun buildNotification(title: String, contextText: String, callNotificationId: Int): Notification {

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.cancel(callNotificationId + INCOMING_CALL_FCM_NOTIFICATION_ID)

        val intent = Intent(this, CallActivity::class.java).apply {

            this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            this.action = CALL_INTENT
            putExtra("call_metadata", callMetadata)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            4,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )


        return NotificationCompat.Builder(this, CALL_CHANNEL_NOTIFICATION_NAME_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // change to app icon or profile pic later
            .setContentTitle(title)
            .setContentText(contextText)
            .setCategory(Notification.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
    }


    private fun startCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = lifecycleScope.launch {
            while (true) {
                delay(1000)
                _callDuration.value += 1
                agoraRepo.updateDuration(_callDuration.value)
            }
        }
    }

    private fun stopCallTimer() {
        callTimerJob?.cancel()
        _callDuration.value = 0L
    }


}