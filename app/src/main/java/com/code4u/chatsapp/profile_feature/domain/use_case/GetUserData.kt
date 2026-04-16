package com.code4u.chatsapp.profile_feature.domain.use_case

import com.code4u.chatsapp.profile_feature.domain.model.CurrentUser
import com.code4u.chatsapp.profile_feature.domain.repository.LocalProfileRepo
import kotlinx.coroutines.flow.Flow

class GetUserData (
    private val localProfileRepo: LocalProfileRepo
) {

    operator fun invoke(): Flow<CurrentUser?> {

        return localProfileRepo.getUserData()
    }
}