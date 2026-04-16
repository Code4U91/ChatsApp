package com.code4u.chatsapp.friend_feature.data.remote_source

import com.code4u.chatsapp.friend_feature.data.local_source.FriendEntity
import com.code4u.chatsapp.friend_feature.domain.model.Friend

fun FriendData.toEntity() = FriendEntity(
    friendId = uid,
    friendName = name,
    photoUrl = photoUrl,
    about = about
)

fun FriendData.toDomain() = Friend(
    name = name,
    photoUrl = photoUrl,
    about = about,
    uid = uid
)