package com.example.chatapp.call_feature.domain.usecase.call_history_case

import com.example.chatapp.call_feature.domain.repository.RemoteCallRepo

class ClearCallHistoryListener (
    private val remoteCallRepo: RemoteCallRepo
) {

    operator fun invoke() {
        remoteCallRepo.clearCallHistoryListener()
    }
}