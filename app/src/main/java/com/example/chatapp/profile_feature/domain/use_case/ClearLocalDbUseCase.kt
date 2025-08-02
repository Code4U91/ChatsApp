package com.example.chatapp.profile_feature.domain.use_case

import com.example.chatapp.profile_feature.domain.repository.LocalProfileRepo

class ClearLocalDbUseCase (
    private val localProfileRepo: LocalProfileRepo
) {

    suspend operator fun invoke(){
        localProfileRepo.clearAllDbTables()
    }
}