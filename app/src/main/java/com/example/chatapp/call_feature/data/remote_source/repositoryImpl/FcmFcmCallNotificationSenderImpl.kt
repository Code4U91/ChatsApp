package com.example.chatapp.call_feature.data.remote_source.repositoryImpl

import android.util.Log
import com.example.chatapp.call_feature.domain.repository.FcmCallNotificationSenderRepo
import com.example.chatapp.core.CallNotificationRequest
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.tasks.await

class FcmFcmCallNotificationSenderImpl (
    private val client: HttpClient,
    private val auth: FirebaseAuth
) : FcmCallNotificationSenderRepo {

    override suspend fun sendCallNotification(request: CallNotificationRequest) {
        try {

            val token = auth.currentUser?.getIdToken(true)?.await()?.token

            if (token == null)
            {
                Log.e("ktor_CALL_SENDER", "No firebase token found")
                return
            }

            val response = client.post("https://chatappktorserver-1.onrender.com/sendCallNotification") {
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
}