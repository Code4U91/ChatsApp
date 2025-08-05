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
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import com.example.chatapp.R
import com.example.chatapp.call_feature.domain.repository.AgoraSetUpRepo
import com.example.chatapp.call_feature.domain.usecase.ringtone_case.RingtoneUseCase
import com.example.chatapp.call_feature.presentation.call_screen.activity.CallActivity
import com.example.chatapp.call_feature.presentation.call_screen.state.CallEvent
import com.example.chatapp.shared.presentation.MainActivity
import com.example.chatapp.core.util.CALL_FCM_NOTIFICATION_CHANNEL_STRING
import com.example.chatapp.core.util.CALL_HISTORY
import com.example.chatapp.core.util.CALL_HISTORY_INTENT
import com.example.chatapp.core.util.CALL_INTENT
import com.example.chatapp.core.util.INCOMING_CALL_FCM_NOTIFICATION_ID
import com.example.chatapp.core.util.MESSAGE_FCM_CHANNEL_STRING
import com.example.chatapp.core.util.MESSAGE_FCM_INTENT
import com.example.chatapp.core.util.MESSAGE_FCM_NOTIFICATION_ID
import com.example.chatapp.core.util.MISSED_CALL_FCM_NOTIFICATION
import com.example.chatapp.core.util.USERS_COLLECTION
import com.example.chatapp.core.model.CallMetadata
import com.example.chatapp.core.model.MessageFcmMetadata
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
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
    lateinit var ringtoneUseCase: RingtoneUseCase

    @Inject
    lateinit var agoraRepo: AgoraSetUpRepo

    @Inject
    @ApplicationContext
    lateinit var context: Context

    private var callStatusListener: ListenerRegistration? = null


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // handle the received FCM message

        if (auth.currentUser == null) return

        val type = message.data["type"]
        val senderId = message.data["senderId"].orEmpty()
        val senderName = message.data["senderName"].orEmpty()
        val imageUrl = message.data["profileImage"]


        when (type) {
            "message" -> {

                val title = message.data["senderName"]
                val body = message.data["message"]
                val chatId = message.data["chatId"].orEmpty()

                if (imageUrl != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val bitmap = getBitmapFromUrl(imageUrl)
                        withContext(Dispatchers.Main) {

                            showNotification(senderName, body, bitmap, senderId, chatId)
                        }
                    }
                } else {
                    showNotification(title, body, null, senderId, chatId)
                }

            }

            "call" -> {

                val callId = message.data["callId"].orEmpty()
                val callType = message.data["callType"].orEmpty()
                val channelName = message.data["channelName"].orEmpty()

                val callNotificationId = channelName.hashCode()

                val callMetadata = CallMetadata(
                    channelName = channelName,
                    uid = "", // current user id, not needed for this case
                    callType = callType,
                    callerName = "",// leaving it empty for now since we have no use of it when the user is a receiver
                    receiverName = senderName,
                    isCaller = false,
                    callReceiverId = senderId, // opposite in case of receiver
                    callDocId = callId,
                    receiverPhoto = imageUrl.orEmpty()
                )

                // If the call ends before use picks or call has already timed out
                listenToCallStatus(callId, notificationId = callNotificationId, onCallMissed = {

                    agoraRepo.declineIncomingCall(true) // may not need
                    // for closing the call screen ui when missed
                    showMissedCallNotification(senderName, callType, callNotificationId)

                }, onIncomingCall = {

                    ringtoneUseCase.playIncomingRingtone()


                    // if the call comes while the app is alive but in background and screen is locked then app crashes
                    if (isAppInForeground()) {

                        showIncomingCallNotification(callMetadata, callNotificationId)

                        val intent = Intent(context, CallActivity::class.java).apply {
                            action = CALL_INTENT
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra("call_metadata", callMetadata)
                        }

                        context.startActivity(intent)

                    } else {

                        showIncomingCallNotification(callMetadata, callNotificationId)

                    }
                })


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

    private fun showIncomingCallNotification(callMetadata: CallMetadata, callNotificationId: Int) {


        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager


        val intentForFullScreen = Intent(this, CallActivity::class.java).apply {
            action = CALL_INTENT
            flags =  Intent.FLAG_ACTIVITY_NEW_TASK
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


        notificationManager.notify(callNotificationId + INCOMING_CALL_FCM_NOTIFICATION_ID, notificationBuilder.build())


    }

    // shows message notification
    @RequiresApi(Build.VERSION_CODES.P)
    private fun showNotification(
        title: String?,
        message: String?,
        profilePic: Bitmap?,
        senderId: String,
        chatId: String
    ) {

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notificationId = chatId.hashCode()


        val personBuilder = Person.Builder()
            .setName(title ?: "User")

        if (profilePic != null) {

            val circularBitMAP = getCircularBitmap(profilePic)
            personBuilder.setIcon(IconCompat.createWithBitmap(circularBitMAP))
        }

        val fcmMessageMetaData = MessageFcmMetadata(
            senderId, chatId
        )

        val fcmMessageIntent = Intent(this, MainActivity::class.java).apply {
            action = MESSAGE_FCM_INTENT
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("fcmMessage", fcmMessageMetaData)

        }

        val fcmMessagePendingIntent = PendingIntent.getActivity(
            this, 1, fcmMessageIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val person = personBuilder.build()

        val messagingStyle = NotificationCompat.MessagingStyle(person)
            .setConversationTitle("New message")
            .addMessage(message ?: "", System.currentTimeMillis(), person)

        val notificationBuilder = NotificationCompat.Builder(this, MESSAGE_FCM_CHANNEL_STRING)
            .setStyle(messagingStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(fcmMessagePendingIntent)
            .setAutoCancel(true)


        notificationManager.notify(notificationId + MESSAGE_FCM_NOTIFICATION_ID, notificationBuilder.build())

    }

    private fun showMissedCallNotification(
        callerName: String,
        callType: String,
        callNotificationId: Int
    ) {

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.cancel(callNotificationId + INCOMING_CALL_FCM_NOTIFICATION_ID)

        val callHistoryIntent = Intent(this, MainActivity::class.java).apply {
            action = CALL_HISTORY_INTENT
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val callHistoryPendingIntent = PendingIntent.getActivity(
            this, 2, callHistoryIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder =
            NotificationCompat.Builder(this, CALL_FCM_NOTIFICATION_CHANNEL_STRING)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Missed call")
                .setContentText("You missed a $callType call from $callerName")
                .setContentIntent(callHistoryPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

        notificationManager.notify(callNotificationId + MISSED_CALL_FCM_NOTIFICATION, notificationBuilder.build())
    }

    private fun listenToCallStatus(
        callId: String,
        notificationId: Int,
        onCallMissed: () -> Unit,
        onIncomingCall: () -> Unit
    ) {

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager



        callStatusListener = firebaseDb.collection(CALL_HISTORY).document(callId)
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val status = snapshot.getString("status") ?: return@addSnapshotListener


                when (status) {
                    "missed" -> {
                        ringtoneUseCase.stopAllRingtone()
                        agoraRepo.updateCallEvent(CallEvent.Ended)
                        onCallMissed()
                        callStatusListener?.remove()
                        callStatusListener = null
                    }

                    "ringing" -> {
                        onIncomingCall()
                    }


                    else -> {
                        ringtoneUseCase.stopAllRingtone()
                        notificationManager.cancel(notificationId + INCOMING_CALL_FCM_NOTIFICATION_ID)
                        callStatusListener?.remove()
                        callStatusListener = null
                    }
                }

            }
    }

    private fun getBitmapFromUrl(imageUrl: String): Bitmap? {

        return try {
            val url = URL(imageUrl)
            BitmapFactory.decodeStream(url.openConnection().getInputStream())
        } catch (_: Exception) {
            null
        }
    }

    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = createBitmap(size, size)

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
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = packageName
        return appProcesses.any { it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && it.processName == packageName }
    }

    override fun onDestroy() {

        callStatusListener?.remove()
        callStatusListener = null
        super.onDestroy()
    }

}