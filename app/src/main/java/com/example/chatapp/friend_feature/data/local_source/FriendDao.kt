package com.example.chatapp.friend_feature.data.local_source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friendEntity: FriendEntity)


    @Query("SELECT * FROM friends ORDER BY LOWER(friendName) ASC")
    fun getFriendList() : Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE friendId = :friendId")
    fun getFriendData(friendId: String) : Flow<FriendEntity?>

    @Query("DELETE FROM friends WHERE friendId IN (:friendId)")
    suspend fun deleteFriend(friendId : Set<String>)
}