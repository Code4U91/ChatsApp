package com.example.chatapp.call_feature.domain.usecase.call_history_case

import com.example.chatapp.call_feature.domain.model.Call
import com.example.chatapp.call_feature.domain.repository.CallRepository

class InsertCallHistoryCase (
    private val callRepository: CallRepository
) {
    suspend operator fun invoke(callData: List<Call>){

        callData.forEach {call ->
            callRepository.insertCallHistory(call)
        }

    }
}
