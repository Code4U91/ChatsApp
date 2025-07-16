package com.example.chatapp.call.domain.usecase.call_history_case

import com.example.chatapp.call.domain.model.Call
import com.example.chatapp.call.domain.repository.CallRepository
import com.example.chatapp.call.domain.repository.RemoteCallRepo
import kotlinx.coroutines.flow.Flow

class GetCallHistoryUseCase(
    private val callRepository: CallRepository,
    private val remoteCallRepo: RemoteCallRepo
) {

    operator fun invoke(local : Boolean) : Flow<List<Call>> {

        return if(local){
            callRepository.getCallHistory()
        } else {

            remoteCallRepo.fetchCallHistory()
        }
    }

}