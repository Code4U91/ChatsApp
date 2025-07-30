package com.example.chatapp.call_feature.domain.repository

import com.example.chatapp.call_feature.data.remote_source.model.CallData
import com.example.chatapp.call_feature.domain.model.Call
import kotlinx.coroutines.flow.Flow

interface LocalCallRepository {


    suspend fun insertCallHistory(calls : List<CallData>)

    fun getCallHistory() : Flow<List<Call>>


}