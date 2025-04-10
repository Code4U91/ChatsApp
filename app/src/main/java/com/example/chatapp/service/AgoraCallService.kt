package com.example.chatapp.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.chatapp.AGORA_ID
import com.example.chatapp.CALL_CHANNEL_NOTIFICATION_ID
import com.example.chatapp.CallMetadata
import com.example.chatapp.MainActivity
import com.example.chatapp.R
import com.example.chatapp.repository.AgoraSetUpRepo
import com.example.chatapp.repository.CallHistoryManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AgoraCallService : LifecycleService() {

    companion object {

        const val NOTIFICATION_ID = 101

    }


    @Inject
    lateinit var agoraRepo: AgoraSetUpRepo

    @Inject
    lateinit var callHistoryManager: CallHistoryManager

    private lateinit var callMetadata: CallMetadata

    private var callId: String? = null
    private var isRemoteUserLeft: Boolean = false

    private var isRemoteUserJoined: Int? = null

    private var serviceJob: Job? = null


    override fun onCreate() {
        super.onCreate()

        agoraRepo.initializeAgora(AGORA_ID)


    }

    @SuppressLint("NewApi")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val metaData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("call_metadata", CallMetadata::class.java)
        } else {
            intent?.getParcelableExtra("call_metadata")
        }


        metaData?.let {

            callMetadata = it

            startForeground(NOTIFICATION_ID, buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)

            lifecycleScope.launch {
                agoraRepo.joinChannel(null, it.channelName, it.callType, it.uid)
            }

            startFlowCollectors()

        }



        return START_STICKY
    }

    private fun startFlowCollectors() {

        serviceJob = lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {

                launch {
                    agoraRepo.isJoined.collect { isJoined ->

                        Log.i("AGORA_CALL_SERVICE", "isJoined : $isJoined")

                        // upload data if the user have joined the channel and the user is an caller
                        if (isJoined && callMetadata.isCaller) {
                            callId = callHistoryManager.uploadCallData(
                                callReceiverId = callMetadata.callReceiverId,
                                callType = callMetadata.callType,
                                channelId = callMetadata.channelName,
                                callStatus = "ringing",
                                callerName = callMetadata.callerName,
                                receiverName = callMetadata.receiverName
                            )
                        }

                    }
                }

                launch {
                    agoraRepo.remoteUserJoined.collect { remoteUser ->

                        Log.i("AGORA_CALL_SERVICE", "RemoteUserJoined: ${remoteUser.toString()}")
                        if (remoteUser != null) {

                            isRemoteUserJoined = remoteUser

                            callId?.let { callDocId ->
                                callHistoryManager.updateCallStatus("ongoing", callDocId)
                            }

                        }

                    }
                }

                launch {
                    agoraRepo.remoteUserLeft.collect { remoteUserLeft ->

                        isRemoteUserLeft = remoteUserLeft
                        if (remoteUserLeft) {
                            stopSelf()
                            Log.i("AGORA_CALL_SERVICE", "Executed StopSelf")
                        }
                        Log.i("AGORA_CALL_SERVICE", "RemoteUserLeft: $remoteUserLeft")
                    }
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        serviceJob?.cancel()

        Log.i("AGORA_CALL_SERVICE", "OnDestroy called")

        if (callMetadata.isCaller) {
            callId?.let { callDocId ->

                val status = if (isRemoteUserJoined != null) "ended" else "missed"

                callHistoryManager.uploadOnCallEnd(status, callDocId)

            }
        }
        agoraRepo.destroy()

    }


    private fun buildNotification(): Notification {

        val intent = Intent(this, MainActivity::class.java).apply {

            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP


            putExtra("call_metadata", callMetadata)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )


        return NotificationCompat.Builder(this, CALL_CHANNEL_NOTIFICATION_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // change to app icon or profile pic later
            .setContentTitle(callMetadata.receiverName)
            .setContentText("Ongoing call")
            .setCategory(Notification.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
    }


}