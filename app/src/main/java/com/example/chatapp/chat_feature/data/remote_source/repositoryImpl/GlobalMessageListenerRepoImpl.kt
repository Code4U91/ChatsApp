package com.example.chatapp.chat_feature.data.remote_source.repositoryImpl

import android.content.Context
import android.util.Log
import com.example.chatapp.chat_feature.data.remote_source.mapper.toDomain
import com.example.chatapp.chat_feature.data.remote_source.model.ChatData
import com.example.chatapp.chat_feature.data.remote_source.model.MessageData
import com.example.chatapp.chat_feature.domain.model.Chat
import com.example.chatapp.chat_feature.domain.model.Message
import com.example.chatapp.chat_feature.domain.repository.GlobalMessageListenerRepo
import com.example.chatapp.core.CHATS_COLLECTION
import com.example.chatapp.core.DEFAULT_PROFILE_PIC
import com.example.chatapp.core.MESSAGE_COLLECTION
import com.example.chatapp.core.USERS_COLLECTION
import com.example.chatapp.core.appInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class GlobalMessageListenerRepoImpl (
    private val auth: FirebaseAuth,
    private val firestoreDb: FirebaseFirestore,
    private val context: Context
) : GlobalMessageListenerRepo {

    private val activeListeners = mutableMapOf<String, ListenerRegistration>()
    private var isUserInChatScreen: (String) -> Boolean = { false }
    private var currentChatList: List<Chat> = emptyList()
    private var chatListListener: ListenerRegistration? = null


    override fun startGlobalMessageListener(
        isUserInChatScreen: (String) -> Boolean,
        onFetchAllActiveChat: (List<Chat>) -> Unit,
        onNewMessages: (String, List<Message>) -> Unit
    ) {

        chatListListener?.remove()

        this.isUserInChatScreen = isUserInChatScreen

        chatListListener?.remove()

        chatListListener = fetchCurrentUserParticipantChats { chatList ->


            if (chatList != currentChatList) {
                currentChatList = chatList
                onFetchAllActiveChat(chatList)
            }


            // remove listeners for chats that no longer exists
            // currently double listeners one global and one when user is on mainChat
            // may be can remove the chatId of the chat where the user is currently present
            removeObsoleteChatListeners(chatList)


            // Add listeners for new chats
            addListenersForNewChats(chatList, onNewMessages)


        }
    }


    private fun addListenersForNewChats(
        chatList: List<Chat>,
        onNewMessages: (String, List<Message>) -> Unit
    ) {
         auth.currentUser?.uid?.let { currentUserId->

             // iterate over every chat from the chatList
             chatList.forEach { (chatId, _) ->

                 // checking for am existing listener, prevents setting up
                 // duplicate listeners
                 if (!activeListeners.containsKey(chatId)) {

                     // setting up listener
                     val listener = firestoreDb.collection(USERS_COLLECTION).document(currentUserId)
                         .collection(CHATS_COLLECTION)
                         .document(chatId)
                         .collection(MESSAGE_COLLECTION)
                         .addSnapshotListener { snapshot, error ->

                             if (error != null) return@addSnapshotListener

                             // Convert each document in the snapshot to a Message object.
                             val newMessages = snapshot?.documents?.mapNotNull { doc ->
                                 doc.toObject(MessageData::class.java)?.toDomain()
                             } ?: emptyList()

                             // update message ui
                             onNewMessages(chatId, newMessages)


                             // Loop Through Each Document for Status Updates
                             val batch = firestoreDb.batch()

                             snapshot?.documents?.forEach docLoop@{ doc ->

                                 val message = doc.toObject(MessageData::class.java) ?: return@docLoop
                                 val currentUserId = auth.currentUser?.uid ?: return@docLoop
                                 val appInstance = context.appInstance()

                                 message.senderId.let {

                                     val senderRef = firestoreDb.collection(USERS_COLLECTION).document(it)
                                         .collection(CHATS_COLLECTION).document(chatId)
                                         .collection(MESSAGE_COLLECTION).document(doc.id)

                                     if (
                                         message.receiverId == currentUserId &&
                                         message.status !in listOf("delivered", "seen")
                                     ) {

                                         val newStatus = if (appInstance.isInForeground && isUserInChatScreen(chatId)) "seen" else "delivered"

                                         batch.update(doc.reference, "status", newStatus)
                                         batch.update(senderRef, "status", newStatus)

                                     }
                                 }

                             }

                             if(snapshot?.documents?.isNotEmpty() ?: return@addSnapshotListener){
                                 batch.commit()
                             }

                         }

                     activeListeners[chatId] = listener  // adding or attaching listeners to the chatId
                 }
             }
         }
    }

    // provides chatId list sorted by the last message activity
    private fun fetchCurrentUserParticipantChats(
        onUpdatedChatList: (List<Chat>) -> Unit
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

                        val otherId = participants.firstOrNull { it != currentUserId } ?: return@mapNotNull null

                        @Suppress("UNCHECKED_CAST")
                        val participantsName = doc.get("participantsName") as? Map<String, String>

                        val otherUserName = participantsName?.get(otherId)
                        val lastMessageTimeStamp = doc.getTimestamp("lastMessageTimeStamp")

                        @Suppress("UNCHECKED_CAST")
                        val participantsPhotoUrl = doc.get("participantsPhotoUrl") as? Map<String, String>
                        val otherUserPhotoUrl = participantsPhotoUrl?.get(otherId)

                        ChatData(
                            chatId = doc.id,
                            otherUserId = otherId,
                            lastMessageTimeStamp = lastMessageTimeStamp,
                            otherUserName = otherUserName.orEmpty(),
                            profileUrl = otherUserPhotoUrl ?: DEFAULT_PROFILE_PIC
                        ).toDomain()
//                        ChatItemData(
//                            chatId = doc.id,
//                            otherUserId = otherId,
//                            lastMessageTimeStamp = lastMessageTimeStamp,
//                            otherUserName = otherUserName.orEmpty(),
//                            profileUrl = otherUserPhotoUrl ?: DEFAULT_PROFILE_PIC
//                        )

                    } ?: emptyList()

                    onUpdatedChatList(chatList)
                }


        }
        return null



    }

    private fun removeObsoleteChatListeners(chatList: List<Chat>) {
        val activeChatIds = chatList.map { it.chatId }
        val chatIdsToRemove = activeListeners.keys.filter { it !in activeChatIds }

        chatIdsToRemove.forEach { chatId ->
            activeListeners[chatId]?.remove()
            activeListeners.remove(chatId)
            Log.d("ChatManager", "Removed listener for chatId: $chatId")
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