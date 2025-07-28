package com.example.chatapp.call_feature.domain.usecase.call_history_case

import com.example.chatapp.call_feature.domain.repository.LocalCallRepository
import com.example.chatapp.call_feature.domain.repository.RemoteCallRepo
import kotlinx.coroutines.flow.distinctUntilChanged

class SyncCallHistoryUseCase(
    private val remoteCallRepo: RemoteCallRepo,
    private val localCallRepository: LocalCallRepository
) {

    suspend operator fun invoke() {

        remoteCallRepo.fetchCallHistory()
            .distinctUntilChanged()
            .collect { domainCallList ->
                localCallRepository.insertCallHistory(
                    calls = domainCallList
                )
            }
    }
}