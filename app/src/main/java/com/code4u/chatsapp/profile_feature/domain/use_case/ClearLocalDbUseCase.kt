package com.code4u.chatsapp.profile_feature.domain.use_case

import com.code4u.chatsapp.profile_feature.domain.repository.LocalProfileRepo

class ClearLocalDbUseCase (
    private val localProfileRepo: LocalProfileRepo
) {

    suspend operator fun invoke(){
        localProfileRepo.clearAllDbTables()
    }
}