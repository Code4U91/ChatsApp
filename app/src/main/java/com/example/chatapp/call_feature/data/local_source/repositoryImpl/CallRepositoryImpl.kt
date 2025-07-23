package com.example.chatapp.call_feature.data.local_source.repositoryImpl

import com.example.chatapp.call_feature.data.local_source.dao.CallHistoryDao
import com.example.chatapp.call_feature.data.local_source.mapper.toDomain
import com.example.chatapp.call_feature.data.local_source.mapper.toEntity
import com.example.chatapp.call_feature.domain.model.Call
import com.example.chatapp.call_feature.domain.repository.CallRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CallRepositoryImpl(
    private val callDao: CallHistoryDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : CallRepository {


    override suspend fun insertCallHistory(call : Call) = withContext(dispatcher) {
        callDao.insertCallHistory(call.toEntity())
    }

    override fun getCallHistory(): Flow<List<Call>> {

        return callDao.getCallHistory().map {
            it.map { entity -> entity.toDomain() }
        }
    }
}