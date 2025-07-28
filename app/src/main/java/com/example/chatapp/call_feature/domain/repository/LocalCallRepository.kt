package com.example.chatapp.call_feature.domain.repository

import com.example.chatapp.call_feature.domain.model.Call
import kotlinx.coroutines.flow.Flow

interface LocalCallRepository {


    suspend fun insertCallHistory(calls : List<Call>)

    fun getCallHistory() : Flow<List<Call>>


}