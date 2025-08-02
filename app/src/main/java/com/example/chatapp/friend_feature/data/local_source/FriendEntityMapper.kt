package com.example.chatapp.friend_feature.data.local_source

import com.example.chatapp.friend_feature.domain.model.Friend

fun FriendEntity.toDomain() = Friend(
    name = friendName,
    photoUrl = photoUrl,
    about = about,
    uid = friendId
)