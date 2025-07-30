package com.example.chatapp.call_feature.data.local_source.repositoryImpl

import com.example.chatapp.call_feature.data.local_source.dao.CallHistoryDao
import com.example.chatapp.call_feature.data.local_source.mapper.toDomain
import com.example.chatapp.call_feature.data.remote_source.mapper.toEntity
import com.example.chatapp.call_feature.data.remote_source.model.CallData
import com.example.chatapp.call_feature.domain.model.Call
import com.example.chatapp.call_feature.domain.repository.LocalCallRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalCallRepositoryImpl(
    private val callDao: CallHistoryDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : LocalCallRepository {


    override suspend fun insertCallHistory(calls : List<CallData>) = withContext(dispatcher) {

        callDao.insertCallHistory(calls.map { it.toEntity() })


    }

    override fun getCallHistory(): Flow<List<Call>> {

        return callDao.getCallHistory().map {
            it.map { entity -> entity.toDomain() }
        }
    }
}