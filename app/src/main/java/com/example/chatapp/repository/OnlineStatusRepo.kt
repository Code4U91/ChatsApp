package com.example.chatapp.repository

import com.example.chatapp.USERS_REF
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
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

    fun listenForOnlineStatus(userId: String, onStatusChanged: (Long) -> Unit): Pair<DatabaseReference, ValueEventListener> {
        val realTimeDbRef = realTimeDb.getReference(USERS_REF).child(userId)


        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val isOnline = snapshot.child("onlineStatus").getValue(Boolean::class.java) ?: false

                if (isOnline) {
                    onStatusChanged(1L)
                } else {
                    val lastSeen = snapshot.child("lastSeen").getValue(Long::class.java) ?: 0L
                    onStatusChanged(lastSeen)

                }

            }

            override fun onCancelled(error: DatabaseError) {
                // if error
            }

        }

        realTimeDbRef.addValueEventListener(listener)
        return Pair(realTimeDbRef, listener)

    }
}