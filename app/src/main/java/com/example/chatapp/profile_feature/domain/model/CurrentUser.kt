package com.example.chatapp.profile_feature.domain.model

data class CurrentUser(
    val uid: String,

    val name: String,
    val photoUrl: String,
    val email: String,
    val about: String,

    val allFcmToken : List<String>
)