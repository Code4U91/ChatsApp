package com.example.chatapp.repository

import android.content.Context
import android.util.Log
import com.example.chatapp.CHATS_COLLECTION
import com.example.chatapp.ChatItemData
import com.example.chatapp.MESSAGE_COLLECTION
import com.example.chatapp.Message
import com.example.chatapp.appInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


// responsible for listening to all active chats, providing active chat lists, fetching and storing new messages
// updating message state, clearing listeners

class GlobalMessageListenerRepo @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestoreDb: FirebaseFirestore,
    @ApplicationContext private val context: Context
) {
    private val activeListeners = mutableMapOf<String, ListenerRegistration>()
    private var isUserInChatScreen: (String) -> Boolean = { false }
    private var currentChatList: List<ChatItemData> = emptyList()
    private var chatListListener: ListenerRegistration? = null


    fun startGlobalMessageListener(
        isUserInChatScreen: (String) -> Boolean,
        onFetchAllActiveChat: (List<ChatItemData>) -> Unit,
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
        chatList: List<ChatItemData>,
        onNewMessages: (String, List<Message>) -> Unit
    ) {

        // iterate over every chat from the chatList
        chatList.forEach { (chatId, _) ->

            // checking for am existing listener, prevents setting up
            // duplicate listeners
            if (!activeListeners.containsKey(chatId)) {

                // setting up listener
                val listener = firestoreDb.collection(CHATS_COLLECTION)
                    .document(chatId)
                    .collection(MESSAGE_COLLECTION)
                    .orderBy("timeStamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->

                        if (error != null) {

                            Log.e("ChatManager", "Message listener error: ${error.message}")
                            return@addSnapshotListener
                        }

                        // Convert each document in the snapshot to a Message object.
                        val newMessages = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(Message::class.java)?.copy(
                                messageId = doc.id
                            )
                        } ?: emptyList()

                        // update message ui
                        onNewMessages(chatId, newMessages)


                        // Loop Through Each Document for Status Updates
                        val batch = firestoreDb.batch()
                        snapshot?.documents?.forEach docLoop@{ doc ->

                            val message = doc.toObject(Message::class.java) ?: return@docLoop
                            val currentUserId = auth.currentUser?.uid ?: return@docLoop
                            val appInstance = context.appInstance()


                            if (
                                message.receiverId == currentUserId &&
                                message.status !in listOf("delivered", "seen")
                            ) {

                                val newStatus =
                                    if (appInstance.isInForeground && isUserInChatScreen(chatId)) {
                                        "seen"
                                    } else {
                                        "delivered"
                                    }

                                batch.update(doc.reference, "status", newStatus)

                                Log.d(
                                    "ChatManager",
                                    "Updated message status to $newStatus for chatId: $chatId"
                                )
                            }
                        }

                        batch.commit()


                    }

                activeListeners[chatId] = listener  // adding or attaching listeners to the chatId
                Log.d("ChatManager", "Added listener for chatId: $chatId")
            }
        }
    }

    // provides chatId list sorted by the last message activity
    private fun fetchCurrentUserParticipantChats(
        onUpdatedChatList: (List<ChatItemData>) -> Unit
    ): ListenerRegistration? {

        val user = auth.currentUser
        if (user != null) {
            val currentUserId = user.uid

            val chatRef = firestoreDb.collection(CHATS_COLLECTION)

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

                        @Suppress("UNCHECKED_CAST")
                        val participantsName = doc.get("participantsName") as? Map<String, String>

                        val otherUserName = participantsName?.get(otherId)


                        val lastMessage = doc.getString("lastMessage")
                        val lastMessageTimeStamp = doc.getTimestamp("lastMessageTimeStamp")

                        ChatItemData(
                            chatId = doc.id,
                            otherUserId = otherId,
                            lastMessage = lastMessage,
                            lastMessageTimeStamp = lastMessageTimeStamp,
                            otherUserName = otherUserName ?: ""
                        )

                    } ?: emptyList()

                    onUpdatedChatList(chatList)
                }
        }
        return null
    }

    private fun removeObsoleteChatListeners(chatList: List<ChatItemData>) {
        val activeChatIds = chatList.map { it.chatId }
        val chatIdsToRemove = activeListeners.keys.filter { it !in activeChatIds }

        chatIdsToRemove.forEach { chatId ->
            activeListeners[chatId]?.remove()
            activeListeners.remove(chatId)
            Log.d("ChatManager", "Removed listener for chatId: $chatId")
        }
    }

    fun clearAllGlobalListeners() {
        activeListeners.values.forEach { it.remove() }
        activeListeners.clear()
        chatListListener?.remove()
        chatListListener = null
        Log.d("ChatManager", "Cleared all global chat listeners")
    }


}



