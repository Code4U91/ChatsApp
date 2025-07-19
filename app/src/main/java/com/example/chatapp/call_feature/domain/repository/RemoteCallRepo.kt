package com.example.chatapp.call_feature.domain.repository

import com.example.chatapp.call_feature.domain.model.Call
import kotlinx.coroutines.flow.Flow

interface RemoteCallRepo {

    fun fetchCallHistory() : Flow<List<Call>>
}