package com.example.chatapp.profile_feature.data.remote_source

data class UserData(
    val uid: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val email: String = "",
    val about: String = "",
    val fcmTokens: List<String> = emptyList()
)