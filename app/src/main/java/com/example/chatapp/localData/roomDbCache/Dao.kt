package com.example.chatapp.localData.roomDbCache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friendEntity: FriendEntity)


    @Query("SELECT * FROM friends ORDER BY LOWER(friendName) ASC")
    fun getFriendList() : Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE friendId = :friendId")
    fun getFriendData(friendId: String) : Flow<FriendEntity?>

}