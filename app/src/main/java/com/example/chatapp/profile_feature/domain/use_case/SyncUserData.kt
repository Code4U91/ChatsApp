package com.example.chatapp.profile_feature.domain.use_case

import com.example.chatapp.auth_feature.domain.usecase.auth_case.AuthUseCase
import com.example.chatapp.profile_feature.domain.repository.LocalProfileRepo
import com.example.chatapp.profile_feature.domain.repository.RemoteProfileRepo
import kotlinx.coroutines.flow.distinctUntilChanged

class SyncUserData(
    private val localProfileRepo: LocalProfileRepo,
    private val remoteProfileRepo: RemoteProfileRepo,
    private val authUseCase: AuthUseCase
) {

    suspend operator fun invoke() {

        authUseCase.getCurrentUser()?.let {
            remoteProfileRepo.fetchUserData()
                .distinctUntilChanged()
                .collect { userData ->
                    userData?.let {
                        localProfileRepo.insertUserData(it)
                    }
                }
        }
    }
}