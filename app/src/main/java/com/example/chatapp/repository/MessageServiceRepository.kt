package com.example.chatapp.repository

import com.example.chatapp.CHATS_COLLECTION
import com.example.chatapp.FRIEND_COLLECTION
import com.example.chatapp.FriendData
import com.example.chatapp.FriendListData
import com.example.chatapp.MESSAGE_COLLECTION
import com.example.chatapp.Message
import com.example.chatapp.USERS_COLLECTION
import com.example.chatapp.UserData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import javax.inject.Inject

class MessageServiceRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestoreDb: FirebaseFirestore,
) {


    private val listenerRegistration = mutableListOf<ListenerRegistration>()

    fun fetchUserData(user: FirebaseUser, onDataChanged: (UserData?) -> Unit) {

        val userRef = firestoreDb.collection(USERS_COLLECTION).document(user.uid)

        val userDataListener = userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {

                val userData = snapshot.toObject(UserData::class.java)
                onDataChanged(userData)
            }
        }

        listenerRegistration.add(userDataListener)
    }


    fun fetchFriendData(
        friendUserId: String,
        updatedFriendData: (FriendData?) -> Unit
    ): ListenerRegistration {
        val userRef = firestoreDb.collection(USERS_COLLECTION).document(friendUserId)


        return userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val friendData = snapshot.toObject(FriendData::class.java)
                updatedFriendData(friendData)
            } else {
                updatedFriendData(null)
            }
        }

    }

    fun addFriend(
        friendUserId: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {

        val user = auth.currentUser

        if (user != null) {

            if (friendUserId != user.uid) {

                // checking if the friend user id is already friend with the current user
                val userFriendRef =
                    firestoreDb.collection(USERS_COLLECTION).document(user.uid).collection(
                        FRIEND_COLLECTION
                    ).document(friendUserId)

                userFriendRef.get()
                    .addOnSuccessListener { friendListDoc ->
                        if (!friendListDoc.exists()) {

                            // Checking if the friends user id exists and have account
                            val userRef =
                                firestoreDb.collection(USERS_COLLECTION).document(friendUserId)
                            userRef.get()
                                .addOnSuccessListener { friendDataDoc ->

                                    if (friendDataDoc.exists()) {

                                        val friendName = friendDataDoc.get("name") as String

                                        val friendData = mapOf(
                                            "friendName" to  friendName
                                        )

                                        // adding a friend
                                        firestoreDb.collection(USERS_COLLECTION)
                                            .document(user.uid)
                                            .collection(FRIEND_COLLECTION)
                                            .document(friendUserId)
                                            .set(friendData)
                                            .addOnSuccessListener { onSuccess() }
                                            .addOnFailureListener { e -> onFailure(e) }
                                    } else {
                                        onFailure(Exception("User with ID \"$friendUserId\" does not exist."))

                                    }
                                }
                                .addOnFailureListener { e ->
                                    onFailure(e)

                                }
                        } else {
                            onFailure(Exception("Entered UserId already exists as your friend"))
                        }
                    }
                    .addOnFailureListener { e -> onFailure(e) }


            } else (
                    onFailure(Exception("You can't add yourself as your own friend."))
                    )
        } else {
            onFailure(Exception("User login error"))
        }


    }

    fun fetchFriendList(onFriendUpdated: (List<FriendListData>, Int) -> Unit): ListenerRegistration? { // added listener

        val user = auth.currentUser
        if (user != null) {

            return firestoreDb.collection(USERS_COLLECTION).document(user.uid)
                .collection(FRIEND_COLLECTION)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {

                        val friendList = snapshot.documents.mapNotNull { doc ->

                            doc.toObject(FriendListData::class.java)?.copy(
                                friendId = doc.id
                            )
                        }

                        onFriendUpdated(friendList, snapshot.size())
                    }
                }

        }

        return null
    }

    fun updateFriendNameOnFriendList(friendName: String, currentUserId: String, friendId: String)
    {
        firestoreDb.collection(USERS_COLLECTION).document(currentUserId).collection(
            FRIEND_COLLECTION).document(friendId)
            .update("friendName", friendName)
    }


    fun deleteFriend(friendId: String) {
        val user = auth.currentUser

        if (user != null) {

            if (friendId != user.uid) {

                // checking if give friendId is friend of the user or not
                val userFriendRef =
                    firestoreDb.collection(USERS_COLLECTION).document(user.uid).collection(
                        FRIEND_COLLECTION
                    ).document(friendId)

                userFriendRef.get()
                    .addOnSuccessListener { friend ->
                        if (friend.exists()) {

                            userFriendRef.delete()
                        }

                    }
            }
        }
    }

    fun sendMessageToSingleUser(
        messageText: String,
        otherUserId: String,
        fetchedChatId: String,
        ) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val currentUserId = currentUser.uid

            val chatId = chatIdCreator(currentUserId, otherUserId, fetchedChatId)


            val chatRef = firestoreDb.collection(CHATS_COLLECTION)
                .document(chatId)

            chatRef.get().addOnSuccessListener { chat ->

                // if the chat doesn't already exists
                if (!chat.exists()) {
                    val chatData = mapOf(
                        "participants" to listOf(currentUser.uid, otherUserId),
                        "lastMessage" to messageText,
                        "lastMessageTimeStamp" to Timestamp.now(),
                        "senderId" to currentUserId,
                        "receiverId" to otherUserId,
                    )

                    chatRef.set(chatData)

                } else {

                    chatRef.update(
                        "lastMessage", messageText,
                        "lastMessageTimeStamp", Timestamp.now()
                    )

                }

                val messageRef = chatRef.collection(MESSAGE_COLLECTION).document()


                val messageItem = mapOf(
                    "senderId" to currentUserId,
                    "receiverId" to otherUserId,
                    "messageContent" to messageText,
                    "timeStamp" to Timestamp.now(),
                    "status" to "sending"
                )

                messageRef.set(messageItem)
                    .addOnSuccessListener {
                        messageRef.update("status", "sent")

                    }

            }

        }
    }

    // causing double fetch, this one used for main chat other one is global listener
    fun fetchMessages(
        friendUserId: String,
        fetchedChatId: String,
        onMessageFetched: (List<Message>) -> Unit
    ): ListenerRegistration? {

        val user = auth.currentUser
        if (user != null) {
            val currentUserId = user.uid

            val chatId = chatIdCreator(currentUserId, friendUserId, fetchedChatId)

            val messagesRef = firestoreDb.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGE_COLLECTION)
                .orderBy("timeStamp", Query.Direction.DESCENDING)


            val fetchMessageListener = messagesRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messageList = snapshot.documents.mapNotNull { doc ->

                        doc.toObject(Message::class.java)?.copy(
                            messageId = doc.id
                        )
                    }

                    onMessageFetched(messageList)
                }


            }

            // might be unnecessary since we clearing it in our onDispose
            // in main screen
            listenerRegistration.add(fetchMessageListener)


            return fetchMessageListener


        } else {
            return null
        }


    }


    fun markMessageAsSeen(chatId: String, currentUserId: String) {

        if (chatId.isNotEmpty()) {
            val chatRef = firestoreDb.collection(CHATS_COLLECTION).document(chatId)

            chatRef.collection(MESSAGE_COLLECTION)
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("status", "delivered")
                .get()
                .addOnSuccessListener { snapShot ->

                    val batch = firestoreDb.batch()

                    snapShot.documents.forEach { doc ->

                        batch.update(doc.reference, "status", "seen")
                        // doc.reference.update("status", "seen")
                    }

                    if (snapShot.documents.isNotEmpty()) {
                        batch.commit()
                    }
                }
        }

    }


        private fun chatIdCreator(
        currentUserId: String,
        friendUserId: String,
        fetchedChatId: String
    ): String {

        if (fetchedChatId.isEmpty()) {
            val chatId = if (currentUserId < friendUserId) {
                "${currentUserId}_$friendUserId"
            } else {
                "${friendUserId}_$currentUserId"
            }

            return chatId
        } else {
            return fetchedChatId
        }

    }

    fun clearMessageListeners() {
        listenerRegistration.forEach { it.remove() }
        listenerRegistration.clear()
    }

}