package com.example.chatapp.localData.roomDbCache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(userEntity: UserEntity)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friendEntity: FriendEntity)


    // Room handles threading for Flow-returning queries, so no need to specifically make it suspend
    @Query ("SELECT * FROM user LIMIT 1")
    fun getUserData() : Flow<UserEntity?>


    @Query("SELECT * FROM friends ORDER BY LOWER(friendName) ASC")
    fun getFriendList() : Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE friendId = :friendId")
    fun getFriendData(friendId: String) : Flow<FriendEntity?>

}