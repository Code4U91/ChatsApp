package com.example.chatapp

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.chatapp.repository.MessageServiceRepository
import com.example.chatapp.repository.OnlineStatusRepo
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ChatsApplication : Application(), DefaultLifecycleObserver {

    @Inject
    lateinit var onlineStatusRepo: OnlineStatusRepo

    @Inject
    lateinit var messageServiceRepository: MessageServiceRepository

    var isInForeground = false


    override fun onCreate() {
        super<Application>.onCreate()

        createNotificationChannel()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner) // can remove contains empty default implementation

        isInForeground = true
        onlineStatusRepo.setOnlineStatusWithDisconnect(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)

        isInForeground = false
        onlineStatusRepo.setOnlineStatusWithDisconnect(false)
    }

    private fun createNotificationChannel() {


        val channel = NotificationChannel(
            CALL_CHANNEL_NOTIFICATION_ID,
            "Call Channel",
            NotificationManager.IMPORTANCE_HIGH
        )


        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)


    }


}