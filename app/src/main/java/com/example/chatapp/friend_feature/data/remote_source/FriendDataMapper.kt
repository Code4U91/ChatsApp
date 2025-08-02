package com.example.chatapp.friend_feature.data.remote_source

import com.example.chatapp.friend_feature.data.local_source.FriendEntity

fun FriendData.toEntity() = FriendEntity(
    friendId = uid,
    friendName = name,
    photoUrl = photoUrl,
    about = about
)