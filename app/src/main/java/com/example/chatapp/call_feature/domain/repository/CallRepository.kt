package com.example.chatapp.call_feature.domain.repository

import com.example.chatapp.call_feature.domain.model.Call
import kotlinx.coroutines.flow.Flow

interface CallRepository {


    suspend fun insertCallHistory(call : Call)

    fun getCallHistory() : Flow<List<Call>>


}