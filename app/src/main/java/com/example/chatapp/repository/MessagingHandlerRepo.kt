package com.example.chatapp.repository

import android.content.Context
import android.util.Log
import com.example.chatapp.CALL_HISTORY
import com.example.chatapp.CHATS_COLLECTION
import com.example.chatapp.CallData
import com.example.chatapp.FRIEND_COLLECTION
import com.example.chatapp.FriendData
import com.example.chatapp.FriendListData
import com.example.chatapp.MESSAGE_COLLECTION
import com.example.chatapp.MessageNotificationRequest
import com.example.chatapp.USERS_COLLECTION
import com.example.chatapp.UserData
import com.example.chatapp.api.FcmNotificationSender
import com.example.chatapp.checkEmailPattern
import com.example.chatapp.localData.dataStore.LocalFcmTokenManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Contains function and listeners for sending message, user data, fcm token, add/delete friend
// call listener and history fetch also is in here


class MessagingHandlerRepo @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestoreDb: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging,
    private val fcmNotificationSender: FcmNotificationSender,
    @ApplicationContext private val context: Context
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

                val fetchedUserData = snapshot.toObject(UserData::class.java)

                @Suppress("UNCHECKED_CAST")
                val fcmTokens = snapshot.get("fcmTokens") as? List<String> ?: emptyList()

                val userData = fetchedUserData?.copy(
                    fcmTokens = fcmTokens
                )
                onDataChanged(userData)
            }
        }

        listenerRegistration.add(userDataListener)
    }


    // function to fetch friend user data
    // fetchUserData and fetchFriendData could be merged into one but currently they put the data in separate
    // data class of its own so not merged yet, might do in future
    fun fetchFriendData(
        friendUserId: String,
        updatedFriendData: (FriendData) -> Unit
    ): ListenerRegistration? {

        if (friendUserId.isNotEmpty()){
            val userRef = firestoreDb.collection(USERS_COLLECTION).document(friendUserId)


            return userRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val friendData = snapshot.toObject(FriendData::class.java)

                    friendData?.let {
                        updatedFriendData(it)
                    }

                }
            }
        }

        return null

    }

    // function to add friend on the users friendList collection
    // user can add friend using either other user id or an email
    fun addFriend(
        friendUserIdEmail: String, onSuccess: () -> Unit, onFailure: (e: Exception) -> Unit
    ) {

        val userId = auth.currentUser?.uid ?: return onFailure(Exception("User not authenticated"))
        val currentUserEmail =
            auth.currentUser?.email ?: return onFailure(Exception("Email is not available"))

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

                val friendData = mapOf(
                    "friendName" to friendName,
                    "friendId" to friendId
                )

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


    fun fetchFriendList(onFriendUpdated: (List<FriendListData>) -> Unit): ListenerRegistration? { // added listener

          auth.currentUser?.uid?.let { userId ->

            return firestoreDb.collection(USERS_COLLECTION).document(userId)
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

                        onFriendUpdated(friendList)
                    }
                }
        }


        return null
    }


    suspend fun sendMessageToSingleUser(
        messageText: String,
        otherUserId: String,
        fetchedChatId: String,
        friendName: String?,
        currentUsername: String?,
        chatId1: String,
    ) {
        auth.currentUser?.uid?.let { currentUserId ->

            val chatId = chatId1.ifEmpty { chatIdCreator(currentUserId, otherUserId, fetchedChatId) }

            val currentTime = Timestamp.now()

            val senderChatRef = firestoreDb.collection(USERS_COLLECTION)
                .document(currentUserId)
                .collection(CHATS_COLLECTION)
                .document(chatId)

            val receiverChatRef = firestoreDb.collection(USERS_COLLECTION)
                .document(otherUserId)
                .collection(CHATS_COLLECTION)
                .document(chatId)

            val mapIdWithName = mapOf(
                currentUserId to currentUsername.orEmpty(),
                otherUserId to friendName.orEmpty()
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
            fcmNotificationSender.sendMessageNotification(request)

        }
    }

    //1. this function and addMessageListenerForChat function at GlobalMessageListerRepo
    // is somewhat similar, proposed to combine them.

    // 2. also currently this marks  all the delivered message as seen
    // proposed to only mark those message which are visible to the user
    // in screen.
    fun markMessageAsSeen(chatId: String, currentUserId: String, friendId: String) {

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


    fun chatIdCreator(
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

    suspend fun updateFcmTokenIfNeeded(savedTokens: List<String>) {
        val user = auth.currentUser ?: return

        val currentToken = firebaseMessaging.token.await()
        // save token locally using datastore
        LocalFcmTokenManager.saveToken(context, currentToken)

        val userDoc = firestoreDb.collection(USERS_COLLECTION).document(user.uid)

        if (currentToken !in savedTokens) {
            //  adding only unique values automatically
            userDoc.update("fcmTokens", FieldValue.arrayUnion(currentToken))
                .addOnSuccessListener { Log.i("FCMCheck", "FCM Token updated: $currentToken") }
                .addOnFailureListener { Log.e("FCMError", "Failed to update token", it) }
        }
    }


    // fetches call history from the firestore database
    fun fetchCallHistory(callHistory: (List<CallData>) -> Unit) {

        auth.currentUser?.let { user ->
            val currentUserId = user.uid

            val callRef = firestoreDb.collection(CALL_HISTORY)
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener { querySnapshot, error ->

                    if (error != null) {
                        Log.e("Firestore", "Error fetching messages", error)
                        return@addSnapshotListener
                    }
                    val callList = querySnapshot?.documents?.mapNotNull { doc ->

                        // multiple .where is causing issue so filtering the rest of data here
                        val status = doc.getString("status") ?: return@mapNotNull null
                        if (status == "ringing" || status == "ongoing") return@mapNotNull null

                        @Suppress("UNCHECKED_CAST")
                        val participants =
                            doc.get("participants") as? List<String> ?: return@mapNotNull null

                        val otherUserId =
                            participants.firstOrNull { it != currentUserId } // pulling other participant

                        @Suppress("UNCHECKED_CAST")
                        val participantsName = doc.get("participantsName") as? Map<String, String>

                        val otherUserName = participantsName?.get(otherUserId)

                        doc.toObject(CallData::class.java)?.copy(
                            callId = doc.id,
                            otherUserName = otherUserName,
                            otherUserId = otherUserId // other participant
                        )

                    } ?: emptyList()

                    callHistory(callList)

                }

            listenerRegistration.add(callRef)
        }


    }

     // only deleting on the current user/user who applies for delete side only
    fun deleteMessage(chatId: String, messageId: Set<String>, currentUserId: String) {

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

    fun deleteFriend(friendId: Set<String>) {

        val userId = auth.currentUser?.uid ?: return

        val userRef = firestoreDb.collection(USERS_COLLECTION).document(userId).collection(
            FRIEND_COLLECTION
        )

        val chunks = friendId.chunked(500)
        for (chunk in chunks) {

            val batch = firestoreDb.batch()
            for (id in chunk) {

                val docRef = userRef.document(id)
                batch.delete(docRef)
            }
            batch.commit()
        }
    }

    // clear all active listeners
    // runs on viewmodel destroy/ sign out
    fun clearMessageListeners() {
        listenerRegistration.forEach { it.remove() }
        listenerRegistration.clear()
    }

}