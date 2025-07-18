package com.example.chatapp.call.domain.repository

import com.example.chatapp.call.domain.model.Call
import kotlinx.coroutines.flow.Flow

interface CallRepository {


    suspend fun insertCallHistory(call : Call)

    fun getCallHistory() : Flow<List<Call>>


}