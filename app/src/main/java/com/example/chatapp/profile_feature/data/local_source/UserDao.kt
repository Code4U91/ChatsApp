package com.example.chatapp.profile_feature.data.local_source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertUser(userEntity: UserEntity)

    // Room handles threading for Flow-returning queries, so no need to specifically make it suspend
    @Query("SELECT * FROM user LIMIT 1")
    fun getUserData() : Flow<UserEntity?>
}