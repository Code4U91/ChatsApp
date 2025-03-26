package com.example.chatapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.chatapp.USERS_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject

class FirebaseMessagingService @Inject constructor(
    private val firebaseDb: FirebaseFirestore,
    private val auth: FirebaseAuth

) : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // handle the received FCM message
        message.notification?.let {

            showNotification(it.title, it.body)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // save the token to firestore

        val user = auth.currentUser
        user?.let {

            firebaseDb.collection(USERS_COLLECTION).document(it.uid)
                .update("fcmToken", token)

            Log.i("FCMCheck", "onNewToken ran : $token")

        }

    }

    private fun showNotification(title: String?, message: String?) {

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                "default_channel",
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager.createNotificationChannel(channel)

        }

        val notificationBuilder = NotificationCompat.Builder(this, "default_channel")
            .setContentTitle(title ?: "New message")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())

    }

}