package com.example.chatapp.chat_feature.data.remote_source.repositoryImpl

import android.util.Log
import com.example.chatapp.chat_feature.domain.repository.FcmMessageNotificationSenderRepo
import com.example.chatapp.core.MessageNotificationRequest
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.tasks.await

class FcmMessageNotificationSenderImpl (
    private val client: HttpClient,
    private val auth: FirebaseAuth
) : FcmMessageNotificationSenderRepo {

    override suspend fun sendMessageNotification(request: MessageNotificationRequest) {
        try {

            val token =  auth.currentUser?.getIdToken(true)?.await()?.token
            if (token == null)
            {
                Log.e("ktor_MESSAGE_SENDER", "No firebase token found")
                return
            }
            val response = client.post("https://chatappktorserver-1.onrender.com/sendMessageNotification") {
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
}