package com.example.chatapp

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.chatapp.auth_feature.domain.repository.OnlineStatusRepo
import com.example.chatapp.chat_feature.MessagingHandlerRepo
import com.example.chatapp.core.CALL_CHANNEL_NOTIFICATION_NAME_ID
import com.example.chatapp.core.CALL_FCM_NOTIFICATION_CHANNEL_STRING
import com.example.chatapp.core.MESSAGE_FCM_CHANNEL_STRING
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ChatsApplication : Application(), DefaultLifecycleObserver {

    @Inject
    lateinit var onlineStatusRepoIml: OnlineStatusRepo

    @Inject
    lateinit var messagingHandlerRepo: MessagingHandlerRepo

    var isInForeground = false


    override fun onCreate() {
        super<Application>.onCreate()

        createNotificationChannel()

        ProcessLifecycleOwner.Companion.get().lifecycle.addObserver(this)
    }

    // setting online status state when user minimizes the app
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner) // can remove contains empty default implementation

        isInForeground = true
        onlineStatusRepoIml.setOnlineStatusWithDisconnect(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)

        isInForeground = false
        onlineStatusRepoIml.setOnlineStatusWithDisconnect(false)
    }

    private fun createNotificationChannel() {


        val callServiceChannel = NotificationChannel(
            CALL_CHANNEL_NOTIFICATION_NAME_ID,
            "Call Channel",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val callFcmChannel = NotificationChannel(
            CALL_FCM_NOTIFICATION_CHANNEL_STRING,
            "Incoming Calls",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Incoming call notification"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        }

        val messageFcmChannel = NotificationChannel(
            MESSAGE_FCM_CHANNEL_STRING,
            "Default Channel",
            NotificationManager.IMPORTANCE_HIGH
        )


        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannels(
            listOf(callServiceChannel, callFcmChannel, messageFcmChannel)
        )


    }


}