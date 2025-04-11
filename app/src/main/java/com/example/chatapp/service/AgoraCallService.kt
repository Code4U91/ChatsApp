package com.example.chatapp.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
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
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
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

    private var isCallDeclined: Boolean = false

    private val listenerRegistration = mutableListOf<ListenerRegistration>()

    private var incomingRingTonePlayer: Ringtone? = null
    private var outgoingRingTonePlayer: MediaPlayer? = null

    private val _callDuration = MutableStateFlow(0L) // in seconds

    private var callTimerJob: Job? = null

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
            // Fallback for older version
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra("call_metadata")
        }


        metaData?.let {

            callMetadata = it

            // support for lower version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID, buildNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } else {

                startForeground(
                    NOTIFICATION_ID, buildNotification()
                )
            }


            if (it.isCaller) // directly join the call if the user is a caller
            {
                val speaker = it.callType == "video"
                playOutGoingRingtone(this, speaker)

                lifecycleScope.launch {
                    agoraRepo.joinChannel(null, it.channelName, it.callType, it.uid)
                }
            } else {
                playIncomingRingtone(this)
            }


            startFlowCollectors(it.callDocId) // callDocId in case needed by the receiver, its firebase doc id where the current
            // active call data is store, may require to update the data directly without querying

        }


        return START_STICKY
    }

    // handles all the coroutine background tasks

    private fun startFlowCollectors(callDocId: String) {

        serviceJob = lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {

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

                            // only caller creates a document
                            // upload data if the user have joined the channel and the user is an caller
                            if (callMetadata.isCaller) {

                                callId = callHistoryManager.uploadCallData(
                                    callReceiverId = callMetadata.callReceiverId,
                                    callType = callMetadata.callType,
                                    channelId = callMetadata.channelName,
                                    callStatus = "ringing",
                                    callerName = callMetadata.callerName,
                                    receiverName = callMetadata.receiverName
                                )

                                // check if the call is declined by the receiver
                                // if the call is declined, receiver updates the document with status "declined"
                                callId?.let {

                                    val listenerForCallDecline =
                                        callHistoryManager.checkAndUpdateCurrentCall(callId = it)
                                        {
                                            isCallDeclined = true
                                            agoraRepo.declineIncomingCall(true) //

                                        }

                                    listenerRegistration.add(listenerForCallDecline)
                                }
                            }


                        } else {

                            // check if the call gets canceled before the receiver picks it
                            // happens if the user calls and before receiver picks the call caller cuts it
                            // in that case close call screen on receiver side
                            // receiver can't join if call isn't picked so join is false
                            // checked by the receiver, i.e isCaller false

                            val listenerForDeclineByCaller =
                                callHistoryManager.checkAndUpdateCurrentCall(callId = callDocId)
                                {

                                    agoraRepo.declineIncomingCall(true) // helper flag to update the ui, listened by callViewmodel

                                }

                            listenerRegistration.add(listenerForDeclineByCaller)

                        }


                    }
                }

                // check if the call became active, i.e other user has joined
                launch {
                    agoraRepo.remoteUserJoined.collect { remoteUser ->

                        Log.i("AGORA_CALL_SERVICE", "RemoteUserJoined: ${remoteUser.toString()}")
                        if (remoteUser != null) {

                            stopAllSounds()
                            startCallTimer()
                            isRemoteUserJoined = remoteUser

                            callId?.let { callDocId ->
                                callHistoryManager.updateCallStatus("ongoing", callDocId)
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

                // if the current user is caller and declines the call update the document with
                // decline status so that the call gets cancelled
                if (!callMetadata.isCaller) {
                    launch {

                        agoraRepo.declineTheCall.collect { callDeclined ->

                            if (callDeclined) {
                                // call is declined by receiver
                                callHistoryManager.updateCallStatus("declined", callDocId)

                            }

                        }
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

        stopAllSounds()
        clearListeners()
        serviceJob?.cancel()

        Log.i("AGORA_CALL_SERVICE", "OnDestroy called")

        // do not update the document data if the user is caller or the user's call got declined by receiver
        if (callMetadata.isCaller && !isCallDeclined) {
            callId?.let { callDocId ->

                val status = if (isRemoteUserJoined != null) "ended" else "missed"

                callHistoryManager.uploadOnCallEnd(status, callDocId)

            }
        }

        // reset the state
        isCallDeclined = false

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


    private fun playIncomingRingtone(context: Context) {

        stopAllSounds()

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        incomingRingTonePlayer = RingtoneManager.getRingtone(context, uri)

        incomingRingTonePlayer?.apply {
            audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            play()
        }
    }

    private fun playOutGoingRingtone(
        context: Context,
        useSpeaker: Boolean = false
    ) {
        stopAllSounds()

        val uri =  Uri.parse("android.resource://${context.packageName}/raw/ringback")
        outgoingRingTonePlayer = MediaPlayer().apply {
            try {

                setDataSource(context, uri)
                isLooping = true
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                prepare()
                configureAudioRouting(context, this, useSpeaker)
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun configureAudioRouting(
        context: Context,
        player: MediaPlayer,
        useSpeaker: Boolean = true
    ) {

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val devices = audioManager.getDevices(
                AudioManager.GET_DEVICES_OUTPUTS
            )

            val targetDevice = devices.firstOrNull { device ->

                if (useSpeaker) {
                    device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                } else {
                    device.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                }
            }

            if (targetDevice != null) {
                player.setPreferredDevice(targetDevice)
            }
        } else {

            // Fallback for older version
            @Suppress("DEPRECATION")
            audioManager.isSpeakerphoneOn = useSpeaker
        }
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


    private fun stopAllSounds() {

        incomingRingTonePlayer?.stop()
        incomingRingTonePlayer = null

        outgoingRingTonePlayer?.apply {
            stop()
            release()
        }
        outgoingRingTonePlayer = null
    }


}