package com.code4u.chatsapp.profile_feature.domain.use_case

import com.code4u.chatsapp.profile_feature.domain.repository.LocalProfileRepo
import com.code4u.chatsapp.profile_feature.domain.repository.RemoteProfileRepo

class FetchUserDataOnce (
    private val remoteProfileRepo: RemoteProfileRepo,
    private val localProfileRepo:  LocalProfileRepo
) {

    suspend operator fun invoke(){

        val userData = remoteProfileRepo.oneTimeUserDataFetch()

        userData?.let {
             localProfileRepo.insertUserData(it)
        }
    }
}