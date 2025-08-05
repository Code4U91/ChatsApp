package com.example.chatapp.chat_feature.data.remote_source.repositoryImpl

import android.content.Context
import android.util.Log
import com.example.chatapp.chat_feature.data.remote_source.model.ChatData
import com.example.chatapp.chat_feature.data.remote_source.model.MessageData
import com.example.chatapp.chat_feature.domain.repository.GlobalMessageListenerRepo
import com.example.chatapp.core.util.CHATS_COLLECTION
import com.example.chatapp.core.util.DEFAULT_PROFILE_PIC
import com.example.chatapp.core.util.MAX_ACTIVE_CHAT_LISTENERS
import com.example.chatapp.core.util.MESSAGE_COLLECTION
import com.example.chatapp.core.util.USERS_COLLECTION
import com.example.chatapp.core.util.appInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class GlobalMessageListenerRepoImpl(
    private val firestoreDb: FirebaseFirestore,
    private val context: Context,
    private val auth: FirebaseAuth
) : GlobalMessageListenerRepo {

    private val activeListeners = mutableMapOf<String, ListenerRegistration>()
    private var chatListListener: ListenerRegistration? = null


    override fun startGlobalMessageListener(
        isUserInChatScreen: (String) -> Boolean
    ): Flow<GlobalChatEvent> = callbackFlow {

        auth.currentUser?.let { user ->

            var currentChatList: List<ChatData> = emptyList()

            chatListListener?.remove()


            chatListListener = fetchCurrentUserParticipantChats(user) { chatList ->

                // Only attach listeners to top active chats
                val topChats = chatList.sortedByDescending { it.lastMessageTimeStamp }.take(
                    MAX_ACTIVE_CHAT_LISTENERS
                )

                if (chatList != currentChatList) {
                    currentChatList = chatList

                    trySend(GlobalChatEvent.AllChatsFetched(chatList))
                }

                removeObsoleteChatListeners(topChats)
                addListenersForNewChats(topChats, user, isUserInChatScreen) { event ->
                    trySend(event)
                }

            }

            awaitClose {
                chatListListener?.remove()
                activeListeners.values.forEach { it.remove() }
                activeListeners.clear()
            }

        } ?: close()

    }

    fun addListenersForNewChats(
        chatList: List<ChatData>,
        user: FirebaseUser,
        isUserInChatScreen: (String) -> Boolean,
        sendEvent: (GlobalChatEvent) -> Unit
    ) {

        for (chat in chatList) {
            val chatId = chat.chatId

            if (activeListeners.containsKey(chatId)) continue

            val listener = firestoreDb.collection(USERS_COLLECTION)
                .document(user.uid)
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

                    updateMessageStatus(snapshot, chatId, user.uid, isUserInChatScreen)

                }

            activeListeners[chatId] = listener

        }
    }

    private fun updateMessageStatus(
        snapshot: QuerySnapshot?,
        chatId: String,
        userId: String,
        isUserInChatScreen: (String) -> Boolean
    ) {
        // Loop Through Each Document for Status Updates
        val batch = firestoreDb.batch()
        val appInstance = context.appInstance()

        snapshot?.documents?.forEach docLoop@{ doc ->

            val message = doc.toObject(MessageData::class.java) ?: return@docLoop

            val senderRef =
                firestoreDb.collection(USERS_COLLECTION).document(message.senderId)
                    .collection(CHATS_COLLECTION).document(chatId)
                    .collection(MESSAGE_COLLECTION).document(doc.id)

            if (
                message.receiverId ==  userId &&
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
        user: FirebaseUser,
        onUpdatedChatList: (List<ChatData>) -> Unit
    ): ListenerRegistration? {


        val chatRef = firestoreDb.collection(USERS_COLLECTION)
            .document(user.uid).collection(CHATS_COLLECTION)

        // unnecessary condition check , since we are storing chats on user collection for each user
        // may remove it later
        return chatRef.whereArrayContains("participants", user.uid)
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    Log.e("Firestore", "Error fetching messages", error)
                    return@addSnapshotListener
                }
                val chatList = snapshots?.documents?.mapNotNull { doc ->

                    @Suppress("UNCHECKED_CAST")
                    val participants =
                        doc.get("participants") as? List<String> ?: return@mapNotNull null

                    val otherId = participants.firstOrNull { it != user.uid }
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

    override suspend fun fetchMessagesOnceForChat(
        isUserInChatScreen: (String) -> Boolean,
        chatId: String
    ): List<MessageData> {

        val currentUserId = auth.currentUser?.uid ?: return emptyList()

        return try {
            val snapShot = firestoreDb.collection(USERS_COLLECTION)
                .document(currentUserId)
                .collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGE_COLLECTION)
                .get()
                .await()

            updateMessageStatus(snapShot, chatId, currentUserId, isUserInChatScreen)

            snapShot.documents.mapNotNull { it.toObject(MessageData::class.java) }
        } catch (e : Exception){
            Log.e("ChatLoad", "failed to load messages for $chatId", e)
            emptyList()

        }

    }


}

sealed class GlobalChatEvent {
    data class AllChatsFetched(val chatList: List<ChatData>) : GlobalChatEvent()
    data class NewMessages(val chatId: String, val messages: List<MessageData>) : GlobalChatEvent()
}