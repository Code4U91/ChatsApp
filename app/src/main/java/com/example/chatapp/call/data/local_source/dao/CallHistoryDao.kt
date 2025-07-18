package com.example.chatapp.call.data.local_source.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.chatapp.call.data.local_source.entity.CallHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallHistoryDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallHistory(callHistoryEntity: CallHistoryEntity)


    @Query("SELECT * FROM callHistory ORDER BY callEndTime DESC")
    fun getCallHistory() : Flow<List<CallHistoryEntity>>
}