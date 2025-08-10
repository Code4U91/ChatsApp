package com.example.chatapp.friend_feature.domain.use_case

data class FriendUseCase(
    val addFriend: AddFriend,
    val deleteFriend: DeleteFriend,
    val getFriendDataById: GetFriendDataById,
    val syncVisibleFriendData: SyncVisibleFriendData,
    val getFriendList: GetFriendList,
    val getAndSaveAllFriendList: GetAndSaveAllFriendList
)
