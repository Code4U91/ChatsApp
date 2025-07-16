package com.example.chatapp.call.domain.usecase.call_case

import android.content.Context
import android.content.Intent
import com.example.chatapp.core.model.CallMetadata
import com.example.chatapp.service.AgoraCallService
import com.google.firebase.auth.FirebaseAuth

class StartCallUseCase (
    private val auth : FirebaseAuth
) {

    operator fun invoke(context : Context, callMetadata: CallMetadata){

        auth.currentUser?.uid?.let {

            val intent = Intent(context, AgoraCallService::class.java).apply {

                putExtra("call_metadata", callMetadata.copy(uid = it))


            }

            context.startForegroundService(intent)
        }

    }
}
