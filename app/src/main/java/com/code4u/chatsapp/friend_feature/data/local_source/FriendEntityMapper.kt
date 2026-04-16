package com.code4u.chatsapp.friend_feature.data.local_source

import com.code4u.chatsapp.friend_feature.domain.model.Friend

fun FriendEntity.toDomain() = Friend(
    name = friendName,
    photoUrl = photoUrl,
    about = about,
    uid = friendId
)