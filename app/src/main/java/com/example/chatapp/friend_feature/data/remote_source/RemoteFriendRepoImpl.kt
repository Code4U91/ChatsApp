package com.example.chatapp.friend_feature.data.remote_source

import android.util.Log
import com.example.chatapp.core.util.FRIEND_COLLECTION
import com.example.chatapp.core.util.USERS_COLLECTION
import com.example.chatapp.core.util.checkEmailPattern
import com.example.chatapp.friend_feature.domain.repository.RemoteFriendRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class RemoteFriendRepoImpl(
    private val firestoreDb: FirebaseFirestore,
    private val auth: FirebaseAuth
) : RemoteFriendRepo {

    private val friendListeners = mutableMapOf<String, ListenerRegistration>()
    private val mutex = Mutex()

    override fun syncOnlyVisibleFriendIds(visibleFriendIds: Set<String>): Flow<FriendData> =
        callbackFlow {

            val cleanedIds = visibleFriendIds.filter { it.isNotBlank() }.toSet()

            Log.i("VISIBLE_FRIEND_REPO",  cleanedIds.toString())

            val user = auth.currentUser

            if (user == null) {
                clearFriendDataListeners()
                close()
                return@callbackFlow
            }

            mutex.withLock {
                val current = friendListeners.keys

                val toRemove = current -  cleanedIds
                val toAdd = cleanedIds - current

                toRemove.forEach { id ->


                    friendListeners[id]?.remove()
                    friendListeners.remove(id)


                }


                toAdd.forEach { id ->

                    Log.i("TO_ADD", id)

                    val listener = firestoreDb.collection(USERS_COLLECTION)
                        .document(id)
                        .addSnapshotListener { snapshot, error ->

                            if (error != null) return@addSnapshotListener

                            val data = snapshot?.toObject(FriendData::class.java)

                            if (data != null) {
                                trySend(data)
                            }


                        }

                    friendListeners[id] = listener

                }
            }

            awaitClose {
                launch {
                    clearFriendDataListeners()
                }

            }
        }

    override suspend fun clearFriendDataListeners() {

        mutex.withLock {
            friendListeners.forEach { (_, registration) ->
                registration.remove()
            }

            friendListeners.clear()
        }

    }

    override suspend fun fetchFriendDataById(id: String): FriendData? {

        return try {

            val snapshot = firestoreDb.collection(USERS_COLLECTION)
                .document(id)
                .get()
                .await()

            if (snapshot.exists()) {
                snapshot.toObject(FriendData::class.java)
            } else {
                null
            }


        } catch (e: Exception) {
            Log.e("FETCH_FRIEND_DATA", "Error fetching friend data for id=$id", e)
            null
        }


    }

    override fun fetchFriendList(): Flow<List<FriendData>> = callbackFlow {

        auth.currentUser?.let { user ->

            val listener = firestoreDb.collection(USERS_COLLECTION).document(user.uid)
                .collection(FRIEND_COLLECTION)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        return@addSnapshotListener
                    }

                    snapshot?.let {
                        val friendList = snapshot.documents.mapNotNull { documentSnapshot ->
                            documentSnapshot.toObject(FriendData::class.java)
                        }

                        trySend(friendList)
                    }
                }

            awaitClose {
                listener.remove()
            }
        } ?: close()
    }

    // function to add friend on the users friendList collection
    // user can add friend using either other user id or an email
    override fun addFriend(
        friendUserIdEmail: String, onSuccess: () -> Unit, onFailure: (e: Exception) -> Unit
    ) {

        auth.currentUser?.let { user ->

            val userId = user.uid

            val currentUserEmail =
                user.email ?: return onFailure(Exception("Email is not available"))

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
        } ?: return onFailure(Exception("User not authenticated"))


    }




    override fun deleteFriend(friendIds: Set<String>) {

        auth.currentUser?.let { user ->

            val userRef = firestoreDb.collection(USERS_COLLECTION)
                .document(user.uid)
                .collection(FRIEND_COLLECTION)

            val chunks = friendIds.chunked(500)
            for (chunk in chunks) {

                val batch = firestoreDb.batch()
                for (id in chunk) {

                    val docRef = userRef.document(id)
                    batch.delete(docRef)
                }
                batch.commit()
            }
        } ?: return

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
                    "name" to friendName,
                    "uid" to friendId
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


}