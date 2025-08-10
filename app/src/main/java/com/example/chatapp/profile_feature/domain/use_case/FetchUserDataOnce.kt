package com.example.chatapp.profile_feature.domain.use_case

import com.example.chatapp.profile_feature.domain.repository.LocalProfileRepo
import com.example.chatapp.profile_feature.domain.repository.RemoteProfileRepo

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