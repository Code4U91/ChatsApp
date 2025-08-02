package com.example.chatapp.friend_feature.data.local_source

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("friends")
data class FriendEntity(

    @PrimaryKey val friendId: String,

    val friendName: String,
    val photoUrl: String,
    val about: String

)