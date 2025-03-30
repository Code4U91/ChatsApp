package com.example.chatapp.repository

import android.util.Log
import com.example.chatapp.CHATS_COLLECTION
import com.example.chatapp.FRIEND_COLLECTION
import com.example.chatapp.FriendData
import com.example.chatapp.FriendListData
import com.example.chatapp.MESSAGE_COLLECTION
import com.example.chatapp.Message
import com.example.chatapp.USERS_COLLECTION
import com.example.chatapp.UserData
import com.example.chatapp.checkEmailPattern
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject

class MessageServiceRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestoreDb: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging
) {


    private val listenerRegistration = mutableListOf<ListenerRegistration>()

    // function to fetch current user data
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


    // function to fetch friend user data
    // fetchUserData and fetchFriendData could be merged into one but currently they put the data in seprate
    // data class of its own so not merged yet, might do in future
    fun fetchFriendData(
        friendUserId: String, updatedFriendData: (FriendData?) -> Unit
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

    // function to add friend on the users friendList collection
    // user can add friend using either other user id or an email
    fun addFriend(
        friendUserIdEmail: String, onSuccess: () -> Unit, onFailure: (e: Exception) -> Unit
    ) {

        val userId = auth.currentUser?.uid ?: return onFailure(Exception("User not authenticated"))
        val currentUserEmail = auth.currentUser?.email ?: return onFailure(Exception("Email is not available"))

        // check if the user is trying to add themselves
        if (friendUserIdEmail == userId || friendUserIdEmail == currentUserEmail) {
            return onFailure(Exception("You can't add yourself as your own friend."))

        }

        if (checkEmailPattern(friendUserIdEmail)) {

            val email = friendUserIdEmail.lowercase()
            // add friend using email id
            addFriendByEmail(email, userId, onSuccess, onFailure)

        } else {
            // add friend using their id
            addFriendById(friendUserIdEmail, userId, onSuccess, onFailure)

        }


    }

    // add friend using provided email address
    private fun addFriendByEmail(
        friendEmail: String,
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {

        // first checking if the user with that email id exists
        firestoreDb.collection(USERS_COLLECTION)
            .whereEqualTo("email", friendEmail)
            .limit(1)
            .get()
            .addOnSuccessListener { snapShot ->

                // snapshot empty them the user with that email does not exists
                if (snapShot.isEmpty) {
                    return@addOnSuccessListener onFailure(Exception("User with email \"$friendEmail\" does not exist."))
                }

                // if the user with that email exists, fetch name to proceed to next step
                val friendDoc = snapShot.documents.first()
                val friendId = friendDoc.id
                val friendName = friendDoc.getString("name") ?: "Name not found"

                // this function checks if the current user is already friend with user currentUser wants to add
                checkAndFriend(
                    userId,
                    friendId,
                    friendName,
                    onSuccess,
                    onFailure
                )
            }
            .addOnFailureListener { e -> onFailure(e) }

    }

    // adds friend to friend list using id
    private fun addFriendById(
        friendId: String,
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {

        // checks if the user with the provided friendId exists
        firestoreDb.collection(USERS_COLLECTION)
            .document(friendId)
            .get()
            .addOnSuccessListener { friendDoc ->

                // if the user with provided email doesn't exists
                if (!friendDoc.exists()) {
                    return@addOnSuccessListener onFailure(Exception("User with Id \"$friendId\" does not exist"))
                }

                // if it exists fetch name and proceed to next step
                val friendName = friendDoc.getString("name") ?: "Name not found"


                // check if user is already friend with it
                checkAndFriend(
                    userId,
                    friendId,
                    friendName,
                    onSuccess,
                    onFailure
                )
            }
            .addOnFailureListener { e -> onFailure(e) }

    }

    // checks if the friendId is already friends with that user or not
    private fun checkAndFriend(
        userId: String,
        friendId: String,
        friendName: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        firestoreDb.collection(USERS_COLLECTION).document(userId)
            .collection(FRIEND_COLLECTION)
            .document(friendId)
            .get()
            .addOnSuccessListener { friendDoc ->

                if (friendDoc.exists()) {
                    return@addOnSuccessListener onFailure(Exception("Entered userId or email already exists as your friend."))
                }

                val friendData = mapOf("friendName" to friendName)

                firestoreDb.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(FRIEND_COLLECTION)
                    .document(friendId)
                    .set(friendData)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure(e) }
            }
            .addOnFailureListener { e -> onFailure(e) }

    }


    fun fetchFriendList(onFriendUpdated: (List<FriendListData>, Int) -> Unit): ListenerRegistration? { // added listener

        val user = auth.currentUser
        if (user != null) {

            return firestoreDb.collection(USERS_COLLECTION).document(user.uid)
                .collection(FRIEND_COLLECTION).addSnapshotListener { snapshot, error ->

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

    fun updateFriendNameOnFriendList(friendName: String, currentUserId: String, friendId: String) {
        firestoreDb.collection(USERS_COLLECTION).document(currentUserId).collection(
            FRIEND_COLLECTION
        ).document(friendId).update("friendName", friendName)
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

                userFriendRef.get().addOnSuccessListener { friend ->
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


            val chatRef = firestoreDb.collection(CHATS_COLLECTION).document(chatId)

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
                        "lastMessage", messageText, "lastMessageTimeStamp", Timestamp.now()
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

                messageRef.set(messageItem).addOnSuccessListener {
                    messageRef.update("status", "sent")

                }

            }

        }
    }

    // causing double fetch, this one used for main chat other one is global listener
    fun fetchMessages(
        friendUserId: String, fetchedChatId: String, onMessageFetched: (List<Message>) -> Unit
    ): ListenerRegistration? {

        val user = auth.currentUser
        if (user != null) {
            val currentUserId = user.uid

            val chatId = chatIdCreator(currentUserId, friendUserId, fetchedChatId)

            val messagesRef = firestoreDb.collection(CHATS_COLLECTION).document(chatId)
                .collection(MESSAGE_COLLECTION).orderBy("timeStamp", Query.Direction.DESCENDING)


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

            chatRef.collection(MESSAGE_COLLECTION).whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("status", "delivered").get().addOnSuccessListener { snapShot ->

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
        currentUserId: String, friendUserId: String, fetchedChatId: String
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

    fun updateFcmTokenIfNeeded(savedToken: String?) {
        val user = auth.currentUser ?: return

        firebaseMessaging.token.addOnSuccessListener { currentToken ->

            val userDoc = firestoreDb.collection(USERS_COLLECTION).document(user.uid)

            savedToken?.let {
                if (it != currentToken) {
                    userDoc.update("fcmToken", currentToken)

                    Log.i("FCMCheck", "onUpdate ran : $currentToken")
                }
            }

        }
    }


    fun clearMessageListeners() {
        listenerRegistration.forEach { it.remove() }
        listenerRegistration.clear()
    }

}