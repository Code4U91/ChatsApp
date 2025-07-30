package com.example.chatapp.core.local_database

import com.example.chatapp.core.FriendData
import com.example.chatapp.core.FriendListData
import com.example.chatapp.localData.roomDbCache.FriendEntity


fun FriendData.toEntity(): FriendEntity {
    return FriendEntity(
        friendId = this.uid,
        friendName = this.name,
        photoUrl = this.photoUrl,
        about = this.about
    )
}

fun FriendEntity.toUi() : FriendData {
    return FriendData(
        uid = this.friendId,
        name = this.friendName,
        photoUrl = this.photoUrl,
        about = this.about
    )
}

fun FriendListData.toEntity() : FriendEntity {
    return FriendEntity(
        friendId = this.friendId,
        friendName = this.friendName,
        photoUrl = "",
        about = ""
    )
}




