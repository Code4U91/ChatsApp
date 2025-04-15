package com.example.chatapp.api

import android.util.Log
import com.example.chatapp.CallNotificationRequest
import com.example.chatapp.MessageNotificationRequest
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FcmNotificationSender @Inject constructor(
    private val client: HttpClient,
    private val auth: FirebaseAuth
) {

    suspend fun sendCallNotification(request: CallNotificationRequest) {
        try {

            val token = getFirebaseIdToken()

            if (token == null)
            {
                Log.e("ktor_CALL_SENDER", "No firebase token found")
                return
            }

            val response = client.post("http://10.0.2.2:8080/sendCallNotification") {
                contentType(ContentType.Application.Json)
                setBody(request)

                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            if (response.status.isSuccess()) {
                Log.e("ktor_CALL_SENDER", "Success")
            } else {
                Log.e("ktor_CALL_SENDER", "Failed : ${response.status}")
            }
        } catch (e: Exception) {
            Log.e("ktor_CALL_SENDER", e.message.toString())
        }
    }

    suspend fun sendMessageNotification(request: MessageNotificationRequest) {
        try {

            val token = getFirebaseIdToken()
            if (token == null)
            {
                Log.e("ktor_CALL_SENDER", "No firebase token found")
                return
            }
            val response = client.post("http://10.0.2.2:8080/sendMessageNotification") {
                contentType(ContentType.Application.Json)
                setBody(request)

                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            if (response.status.isSuccess()) {
                Log.e("ktor_MESSAGE_SENDER", "Success")
            } else {
                Log.e("ktor_MESSAGE_SENDER", "Failed : ${response.status}")
            }
        } catch (e: Exception) {
            Log.e("ktor_MESSAGE_SENDER", e.message.toString())
        }
    }

    private suspend fun getFirebaseIdToken(): String? {
        return auth.currentUser?.getIdToken(true)?.await()?.token
    }
}