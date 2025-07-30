package com.example.chatapp.call_feature.domain.usecase.call_history_case

import com.example.chatapp.call_feature.domain.model.Call
import com.example.chatapp.call_feature.domain.repository.LocalCallRepository
import kotlinx.coroutines.flow.Flow

class GetCallHistoryUseCase(
    private val localCallRepository: LocalCallRepository
) {

    operator fun invoke() : Flow<List<Call>> {

        return localCallRepository.getCallHistory()
    }

}