package com.example.chatapp.profile_feature.domain.use_case

import com.example.chatapp.profile_feature.domain.model.CurrentUser
import com.example.chatapp.profile_feature.domain.repository.LocalProfileRepo
import kotlinx.coroutines.flow.Flow

class GetUserData (
    private val localProfileRepo: LocalProfileRepo
) {

    operator fun invoke(): Flow<CurrentUser?> {

        return localProfileRepo.getUserData()
    }
}