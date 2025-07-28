package com.example.chatapp.call_feature.domain.usecase.call_history_case

import com.example.chatapp.call_feature.domain.model.Call
import com.example.chatapp.call_feature.domain.repository.LocalCallRepository
import com.example.chatapp.call_feature.domain.repository.RemoteCallRepo
import kotlinx.coroutines.flow.Flow

class GetCallHistoryUseCase(
    private val localCallRepository: LocalCallRepository,
    private val remoteCallRepo: RemoteCallRepo
) {

    operator fun invoke(local : Boolean) : Flow<List<Call>> {

        return if(local){
            localCallRepository.getCallHistory()
        } else {

            remoteCallRepo.fetchCallHistory()
        }
    }

}