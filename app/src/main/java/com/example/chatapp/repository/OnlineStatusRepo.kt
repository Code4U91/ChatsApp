package com.example.chatapp.repository

import com.example.chatapp.USERS_REF
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import javax.inject.Inject

class OnlineStatusRepo @Inject constructor(
    private val auth: FirebaseAuth,
    private val realTimeDb: FirebaseDatabase
) {

    fun setOnlineStatusWithDisconnect(status: Boolean) {
        val user = auth.currentUser
        if (user != null) {
            val realTimeDbRef =
                realTimeDb.getReference(USERS_REF).child(user.uid)

            realTimeDbRef.onDisconnect().setValue(
                mapOf(
                    "onlineStatus" to false,
                    "lastSeen" to ServerValue.TIMESTAMP
                )
            )

            val statusData = mapOf(
                "onlineStatus" to status,
                "lastSeen" to ServerValue.TIMESTAMP
            )

            realTimeDbRef.setValue(statusData) // online


        }
    }
}