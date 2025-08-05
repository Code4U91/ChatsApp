package com.example.chatapp.chat_feature.data.remote_source.repositoryImpl

import android.util.Log
import com.example.chatapp.chat_feature.data.remote_source.model.MessageNotificationRequest
import com.example.chatapp.chat_feature.domain.repository.MessageHandlerRepo
import com.example.chatapp.chat_feature.domain.repository.FcmMessageNotificationSenderRepo
import com.example.chatapp.core.util.CHATS_COLLECTION
import com.example.chatapp.core.util.MESSAGE_COLLECTION
import com.example.chatapp.core.util.USERS_COLLECTION
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class MessagingHandlerRepoImpl(
    private val auth: FirebaseAuth,
    private val firestoreDb: FirebaseFirestore,
    private val fcmMessageNotificationSenderRepo: FcmMessageNotificationSenderRepo
) : MessageHandlerRepo {


    override suspend fun sendMessageToSingleUser(
        messageText: String,
        otherUserId: String,
        fetchedChatId: String,
        friendName: String,
        currentUsername: String
    ) {
        auth.currentUser?.uid?.let { currentUserId ->

            val chatId =
                fetchedChatId.ifEmpty { chatIdCreator(currentUserId, otherUserId) }

            val currentTime = Timestamp.Companion.now()

            val senderChatRef = firestoreDb.collection(USERS_COLLECTION)
                .document(currentUserId)
                .collection(CHATS_COLLECTION)
                .document(chatId)

            val receiverChatRef = firestoreDb.collection(USERS_COLLECTION)
                .document(otherUserId)
                .collection(CHATS_COLLECTION)
                .document(chatId)

            val mapIdWithName = mapOf(
                currentUserId to currentUsername,
                otherUserId to friendName
            )

            val chatData = mapOf(
                "participants" to listOf(currentUserId, otherUserId),
                "lastMessageTimeStamp" to currentTime,
                "participantsName" to mapIdWithName
            )

            val messageRefId = senderChatRef.collection(MESSAGE_COLLECTION).document().id

            val senderMessageRef = senderChatRef.collection(MESSAGE_COLLECTION)
                .document(messageRefId)

            val receiverMessageRef = receiverChatRef.collection(MESSAGE_COLLECTION)
                .document(messageRefId)

            val messageData = mapOf(
                "messageId" to messageRefId,
                "senderId" to currentUserId,
                "receiverId" to otherUserId,
                "messageContent" to messageText,
                "timeStamp" to currentTime,
                "status" to "sending",
                "chatId" to chatId
            )

            val request = MessageNotificationRequest(
                senderId = currentUserId,
                receiverId = otherUserId,
                messageId = messageRefId,
                chatId = senderChatRef.id
            )

            firestoreDb.runBatch { batch ->

                // create/update chat metadata
                batch.set(senderChatRef, chatData, SetOptions.merge())
                batch.set(receiverChatRef, chatData, SetOptions.merge())

                // create message collection and put the message in there
                batch.set(senderMessageRef, messageData)
                batch.set(receiverMessageRef, messageData)
            }.await()

            val statusUpdate = mapOf("status" to "sent")

            // update the message status when the message is sent successfully
            firestoreDb.runBatch { batch ->
                batch.update(senderMessageRef, statusUpdate)
                batch.update(receiverMessageRef, statusUpdate)
            }.await()

            // send notification if sent successfully
             fcmMessageNotificationSenderRepo.sendMessageNotification(request)


        }
    }

    //1. this function and addMessageListenerForChat function at GlobalMessageListerRepo
    // is somewhat similar, proposed to combine them.

    // 2. also currently this marks  all the delivered message as seen
    // proposed to only mark those message which are visible to the user
    // in screen.
    override fun markMessageAsSeen(chatId: String, currentUserId: String, friendId: String) {

        if (chatId.isNotEmpty()) {

            val currentUserChatRef =
                firestoreDb.collection(USERS_COLLECTION).document(currentUserId)
                    .collection(CHATS_COLLECTION).document(chatId)

            val newStatus = mapOf("status" to "seen")

            val batch = firestoreDb.batch()


            currentUserChatRef.collection(MESSAGE_COLLECTION)
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("status", "delivered").get().addOnSuccessListener { snapShot ->
                    snapShot.documents.forEach { doc ->

                        if (doc.exists()) {
                            val friendChatRef =
                                firestoreDb.collection(USERS_COLLECTION).document(friendId)
                                    .collection(CHATS_COLLECTION).document(chatId)
                                    .collection(MESSAGE_COLLECTION)
                                    .document(doc.id)

                            batch.update(friendChatRef, newStatus)
                            batch.update(doc.reference, newStatus)
                        }
                    }
                    if (snapShot.documents.isNotEmpty()) {
                        batch.commit()
                    }
                }
        }

    }


    override fun chatIdCreator(
        currentUserId: String, friendUserId: String
    ): String {

        val chatId = if (currentUserId < friendUserId) {
            "${currentUserId}_$friendUserId"
        } else {
            "${friendUserId}_$currentUserId"
        }

        return chatId

    }

    // only deleting on the current user/user who applies for delete side only
    override fun deleteMessage(chatId: String, messageId: Set<String>, currentUserId: String) {

        val dbRef =
            firestoreDb.collection(USERS_COLLECTION)
                .document(currentUserId)
                .collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGE_COLLECTION)

        val chunks = messageId.chunked(500)
        for (chunk in chunks) {

            val batch = firestoreDb.batch()
            for (id in chunk) {

                val docRef = dbRef.document(id)
                batch.delete(docRef)
            }
            batch.commit().addOnFailureListener {
                Log.e("FIREStore_DEBUG", "Batch delete failed: ${it.message}")
            }
        }

    }

}