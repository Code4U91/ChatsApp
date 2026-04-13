package com.code4u.chatsapp.profile_feature.data.remote_source

import com.code4u.chatsapp.profile_feature.data.local_source.UserEntity

fun UserData.toEntity() = UserEntity(
    uid = uid,
    name = name,
    photoUrl = photoUrl,
    email = email,
    about = about,
    allFcmToken = fcmTokens
)