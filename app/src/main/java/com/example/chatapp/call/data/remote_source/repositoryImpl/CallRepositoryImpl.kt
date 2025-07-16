package com.example.chatapp.call.data.remote_source.repositoryImpl

import com.example.chatapp.call.data.local_source.dao.CallHistoryDao
import com.example.chatapp.call.data.local_source.mapper.toDomain
import com.example.chatapp.call.data.local_source.mapper.toEntity
import com.example.chatapp.call.domain.model.Call
import com.example.chatapp.call.domain.repository.CallRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CallRepositoryImpl(
    private val callDao: CallHistoryDao
) : CallRepository {


    override suspend fun insertCallHistory(call : Call) {

        callDao.insertCallHistory(call.toEntity())
    }

    override fun getCallHistory(): Flow<List<Call>>{

        return callDao.getCallHistory().map {
            it.map { entity -> entity.toDomain() }
        }
    }
}