package com.example.chatapp.service

import android.app.ActivityManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.example.chatapp.CALL_FCM_NOTIFICATION_CHANNEL_STRING
import com.example.chatapp.CALL_FCM_NOTIFICATION_ID
import com.example.chatapp.CallEventHandler
import com.example.chatapp.CallMetadata
import com.example.chatapp.MESSAGE_FCM_CHANNEL_STRING
import com.example.chatapp.MESSAGE_FCM_NOTIFICATION_ID
import com.example.chatapp.MainActivity
import com.example.chatapp.R
import com.example.chatapp.USERS_COLLECTION
import com.example.chatapp.repository.CallRingtoneManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject


@AndroidEntryPoint
class FirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var firebaseDb: FirebaseFirestore

    @Inject
    lateinit var callRingtoneManager: CallRingtoneManager


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // handle the received FCM message

        if (auth.currentUser == null) return

        val type = message.data["type"]
        val title = message.data["senderName"]
        val body = message.data["message"]
        val imageUrl = message.data["profileImage"]

        Log.i("FCMCheck", "title: $title, body: $body, profileUrl: $imageUrl")

        when (type) {
            "message" -> {
                if (imageUrl != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val bitmap = getBitmapFromUrl(imageUrl)
                        withContext(Dispatchers.Main) {
                            showNotification(title, body, bitmap)
                        }
                    }
                } else {
                    showNotification(title, body, null)
                }

            }

            "call" -> {

                val callId = message.data["callId"] ?: ""
                val callType = message.data["callType"] ?: ""
                val senderId = message.data["senderId"] ?: ""
                val senderName = message.data["senderName"] ?: ""
                val channelName = message.data["channelName"] ?: ""

                val callMetadata = CallMetadata(
                    channelName = channelName,
                    uid = "", // current user id, not needed for this case
                    callType = callType,
                    callerName = senderName,
                    receiverName = "",
                    isCaller = false,
                    callReceiverId = senderId, // opposite in case of receiver
                    callDocId = callId
                )

                callRingtoneManager.playIncomingRingtone()
                if (isAppInForeground()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        CallEventHandler.incomingCall.emit(callMetadata)
                    }
                } else {


                    showIncomingCallNotification(callMetadata)


                }
            }

            else -> {}

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

    private fun showIncomingCallNotification(callMetadata: CallMetadata) {


        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//
//
//        val channel = NotificationChannel(
//            CALL_FCM_NOTIFICATION_CHANNEL_STRING,
//            "Incoming Calls",
//            NotificationManager.IMPORTANCE_HIGH
//        ).apply {
//            description = "Incoming call notification"
//            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
//
//        }
//
//        notificationManager.createNotificationChannel(channel)


        val intentForFullScreen = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("call_metadata", callMetadata)
        }

        val fullScreenIntent = PendingIntent.getActivity(
            this, 0, intentForFullScreen,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder =
            NotificationCompat.Builder(this, CALL_FCM_NOTIFICATION_CHANNEL_STRING)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Tap to return to the call")
                .setContentText("${callMetadata.callerName} is ${callMetadata.callType} calling you")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(
                    fullScreenIntent,
                    true
                ) // ONLY SHOWS FULL SCREEN or launches the activity/call ui if the phone is locked
                .setAutoCancel(true)


        notificationManager.notify(CALL_FCM_NOTIFICATION_ID, notificationBuilder.build())


    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun showNotification(title: String?, message: String?, profilePic: Bitmap?) {

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        val personBuilder = Person.Builder()
            .setName(title ?: "User")

        if (profilePic != null) {

            val circularBitMAP = getCircularBitmap(profilePic)
            personBuilder.setIcon(IconCompat.createWithBitmap(circularBitMAP))
        }

        val person = personBuilder.build()

        val messagingStyle = NotificationCompat.MessagingStyle(person)
            .setConversationTitle("New message")
            .addMessage(message ?: "", System.currentTimeMillis(), person)

        val notificationBuilder = NotificationCompat.Builder(this, MESSAGE_FCM_CHANNEL_STRING)
            .setStyle(messagingStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)


        notificationManager.notify(MESSAGE_FCM_NOTIFICATION_ID, notificationBuilder.build())

    }

    private fun getBitmapFromUrl(imageUrl: String): Bitmap? {

        Log.i("FCMCheck", imageUrl)
        return try {
            val url = URL(imageUrl)
            BitmapFactory.decodeStream(url.openConnection().getInputStream())
        } catch (e: Exception) {
            null
        }
    }

    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = android.graphics.Canvas(output)
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
        }

        val rect = android.graphics.Rect(0, 0, size, size)
        val rectF = android.graphics.RectF(rect)

        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawOval(rectF, paint)

        paint.xfermode =
            android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, null, rect, paint)

        return output
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = packageName
        return appProcesses.any { it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && it.processName == packageName }
    }

}