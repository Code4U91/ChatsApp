package com.code4u.chatsapp.call_feature.domain.repository

import com.code4u.chatsapp.call_feature.data.remote_source.model.CallData
import kotlinx.coroutines.flow.Flow

interface RemoteCallRepo {

    fun fetchCallHistory() : Flow<List<CallData>>

    fun clearCallHistoryListener()
}