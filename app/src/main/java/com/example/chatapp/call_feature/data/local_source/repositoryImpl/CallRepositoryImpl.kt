package com.example.chatapp.call_feature.data.local_source.repositoryImpl

import com.example.chatapp.call_feature.data.local_source.dao.CallHistoryDao
import com.example.chatapp.call_feature.data.local_source.mapper.toDomain
import com.example.chatapp.call_feature.domain.model.Call
import com.example.chatapp.call_feature.domain.repository.CallRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CallRepositoryImpl(
    private val callDao: CallHistoryDao
) : CallRepository {


    override suspend fun insertCallHistory(call : Call) {

    }

    override fun getCallHistory(): Flow<List<Call>> {

        return callDao.getCallHistory().map {
            it.map { entity -> entity.toDomain() }
        }
    }
}