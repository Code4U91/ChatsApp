package com.example.chatapp.call.domain.repository

import com.example.chatapp.call.domain.model.Call
import kotlinx.coroutines.flow.Flow

interface RemoteCallRepo {

    fun fetchCallHistory() : Flow<List<Call>>
}