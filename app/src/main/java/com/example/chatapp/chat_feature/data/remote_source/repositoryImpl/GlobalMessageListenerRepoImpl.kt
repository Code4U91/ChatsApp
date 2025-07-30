package com.example.chatapp.chat_feature.data.remote_source.repositoryImpl

import android.content.Context
import android.util.Log
import com.example.chatapp.chat_feature.data.remote_source.model.ChatData
import com.example.chatapp.chat_feature.data.remote_source.model.MessageData
import com.example.chatapp.chat_feature.domain.repository.GlobalMessageListenerRepo
import com.example.chatapp.core.CHATS_COLLECTION
import com.example.chatapp.core.DEFAULT_PROFILE_PIC
import com.example.chatapp.core.MESSAGE_COLLECTION
import com.example.chatapp.core.USERS_COLLECTION
import com.example.chatapp.core.appInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class GlobalMessageListenerRepoImpl(
    private val auth: FirebaseAuth,
    private val firestoreDb: FirebaseFirestore,
    private val context: Context
) : GlobalMessageListenerRepo {

    private val activeListeners = mutableMapOf<String, ListenerRegistration>()
    private var chatListListener: ListenerRegistration? = null


    override fun startGlobalMessageListener(
        isUserInChatScreen: (String) -> Boolean
    ): Flow<GlobalChatEvent> = callbackFlow {

        var currentChatList: List<ChatData> = emptyList()

        chatListListener?.remove()

        chatListListener = fetchCurrentUserParticipantChats { chatList ->


            if (chatList != currentChatList) {
                currentChatList = chatList

                trySend(GlobalChatEvent.AllChatsFetched(chatList))
            }

            removeObsoleteChatListeners(chatList)
            addListenersForNewChats(chatList, isUserInChatScreen){ event ->
                trySend(event)
            }

        }

        awaitClose {
            chatListListener?.remove()
            activeListeners.values.forEach { it.remove() }
            activeListeners.clear()
        }

    }

    fun addListenersForNewChats(
        chatList: List<ChatData>,
        isUserInChatScreen: (String) -> Boolean,
        sendEvent: (GlobalChatEvent) -> Unit
    ) {
        auth.currentUser?.uid?.let { currentUserId ->

            for (chat in chatList) {
                val chatId = chat.chatId

                if (activeListeners.containsKey(chatId)) continue

                val listener = firestoreDb.collection(USERS_COLLECTION).document(currentUserId)
                    .collection(CHATS_COLLECTION)
                    .document(chatId)
                    .collection(MESSAGE_COLLECTION)
                    .addSnapshotListener { snapshot, error ->

                        if (error != null) return@addSnapshotListener

                        // Convert each document in the snapshot to a Message object.
                        val newMessages = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(MessageData::class.java)
                        } ?: emptyList()

                        sendEvent(GlobalChatEvent.NewMessages(chatId, newMessages))

                        updateMessageStatus(snapshot, chatId, isUserInChatScreen)

                    }

                activeListeners[chatId] = listener

            }
        }
    }

    private fun updateMessageStatus(
        snapshot: QuerySnapshot?,
        chatId : String,
        isUserInChatScreen: (String) -> Boolean
    ){
        // Loop Through Each Document for Status Updates
        val batch = firestoreDb.batch()
        val currentUserId = auth.currentUser?.uid ?: return
        val appInstance = context.appInstance()

        snapshot?.documents?.forEach docLoop@{ doc ->

            val message = doc.toObject(MessageData::class.java) ?: return@docLoop

            val senderRef =
                firestoreDb.collection(USERS_COLLECTION).document(message.senderId)
                    .collection(CHATS_COLLECTION).document(chatId)
                    .collection(MESSAGE_COLLECTION).document(doc.id)

            if (
                message.receiverId == currentUserId &&
                message.status !in listOf("delivered", "seen")
            ) {

                val newStatus =
                    if (appInstance.isInForeground && isUserInChatScreen(chatId)
                    ) "seen" else "delivered"

                batch.update(doc.reference, "status", newStatus)
                batch.update(senderRef, "status", newStatus)

            }

        }

        if (snapshot?.documents?.isNotEmpty() == true) {
            batch.commit()
        }

    }


    // provides chatId list sorted by the last message activity
    private fun fetchCurrentUserParticipantChats(
        onUpdatedChatList: (List<ChatData>) -> Unit
    ): ListenerRegistration? {

        auth.currentUser?.uid?.let { currentUserId ->

            val chatRef = firestoreDb.collection(USERS_COLLECTION)
                .document(currentUserId).collection(CHATS_COLLECTION)

            // unnecessary condition check , since we are storing chats on user collection for each user
            // may remove it later
            return chatRef.whereArrayContains("participants", currentUserId)
                .addSnapshotListener { snapshots, error ->

                    if (error != null) {
                        Log.e("Firestore", "Error fetching messages", error)
                        return@addSnapshotListener
                    }
                    val chatList = snapshots?.documents?.mapNotNull { doc ->

                        @Suppress("UNCHECKED_CAST")
                        val participants =
                            doc.get("participants") as? List<String> ?: return@mapNotNull null

                        val otherId = participants.firstOrNull { it != currentUserId }
                            ?: return@mapNotNull null

                        @Suppress("UNCHECKED_CAST")
                        val participantsName = doc.get("participantsName") as? Map<String, String>

                        val otherUserName = participantsName?.get(otherId)
                        val lastMessageTimeStamp = doc.getTimestamp("lastMessageTimeStamp")

                        @Suppress("UNCHECKED_CAST")
                        val participantsPhotoUrl =
                            doc.get("participantsPhotoUrl") as? Map<String, String>
                        val otherUserPhotoUrl = participantsPhotoUrl?.get(otherId)

                        ChatData(
                            chatId = doc.id,
                            otherUserId = otherId,
                            lastMessageTimeStamp = lastMessageTimeStamp,
                            otherUserName = otherUserName.orEmpty(),
                            profileUrl = otherUserPhotoUrl ?: DEFAULT_PROFILE_PIC
                        )
                    } ?: emptyList()

                    onUpdatedChatList(chatList)
                }


        }
        return null


    }

    private fun removeObsoleteChatListeners(newList: List<ChatData>) {

        val newIds = newList.map { it.chatId }.toSet()
        val toRemove = activeListeners.keys.filterNot { newIds.contains(it) }

        toRemove.forEach {
            activeListeners[it]?.remove()
            activeListeners.remove(it)
        }
    }

    override fun clearAllGlobalListeners() {
        activeListeners.values.forEach { it.remove() }
        activeListeners.clear()
        chatListListener?.remove()
        chatListListener = null
        Log.d("ChatManager", "Cleared all global chat listeners")
    }


}

sealed class GlobalChatEvent {
    data class AllChatsFetched(val chatList: List<ChatData>) : GlobalChatEvent()
    data class NewMessages(val chatId: String, val messages: List<MessageData>) : GlobalChatEvent()
}