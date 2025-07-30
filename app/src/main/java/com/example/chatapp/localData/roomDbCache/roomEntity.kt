package com.example.chatapp.localData.roomDbCache

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("friends")
data class FriendEntity(

    @PrimaryKey val friendId: String,

    val friendName: String,
    val photoUrl: String,
    val about: String

)