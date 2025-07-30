package com.example.chatapp.profile_feature.data.local_source

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("user")
data class UserEntity(
    @PrimaryKey val uid: String,

    val name: String,
    val photoUrl: String,
    val email: String,
    val about: String,

    val allFcmToken : List<String>
)