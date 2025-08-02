package com.example.chatapp.profile_feature.domain.use_case

import com.example.chatapp.profile_feature.domain.repository.RemoteProfileRepo

class ClearUserDataListener (
    private val remoteProfileRepo: RemoteProfileRepo
) {
    operator fun invoke(){
        remoteProfileRepo.clearProfileUserDataListener()
    }
}