package com.example.chatapp.auth_feature.data.repositoryIml

import com.example.chatapp.auth_feature.domain.repository.OnlineStatusRepo
import com.example.chatapp.core.USERS_REF
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener

class OnlineStatusRepoIml (
    private val auth: FirebaseAuth,
    private val realTimeDb: FirebaseDatabase
) : OnlineStatusRepo {

    override fun activeChatUpdate(chatId: String) {
        val userId = auth.currentUser?.uid ?: return

        val realTimeDb = realTimeDb.getReference(USERS_REF).child(userId)

        val activeChatId = mapOf(
            "currentChattingWith" to chatId
        )
        realTimeDb.updateChildren(activeChatId)
    }

    override fun setOnlineStatusWithDisconnect(status: Boolean, chatId: String) {
        val user = auth.currentUser
        if (user != null) {
            val realTimeDbRef =
                realTimeDb.getReference(USERS_REF).child(user.uid)

            realTimeDbRef.onDisconnect().setValue(
                mapOf(
                    "onlineStatus" to false,
                    "currentChattingWith" to chatId,
                    "lastSeen" to ServerValue.TIMESTAMP
                )
            )

            val statusData = mapOf(
                "onlineStatus" to status,
                "currentChattingWith" to chatId,
                "lastSeen" to ServerValue.TIMESTAMP
            )

            realTimeDbRef.updateChildren(statusData)


        }
    }

    override fun listenForOnlineStatus(
        userId: String,
        onStatusChanged: (Long) -> Unit
    ): Pair<DatabaseReference, ValueEventListener> {
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