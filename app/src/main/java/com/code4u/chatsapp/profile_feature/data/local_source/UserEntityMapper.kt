package com.code4u.chatsapp.profile_feature.data.local_source

import com.code4u.chatsapp.profile_feature.domain.model.CurrentUser

fun UserEntity.toDomain() : CurrentUser {

    return CurrentUser(
        uid = uid,
        name = name,
        photoUrl = photoUrl,
        email = email,
        about = about,
        allFcmToken = allFcmToken
    )
}