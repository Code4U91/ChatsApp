package com.example.chatapp.profile_feature.domain.use_case

import com.example.chatapp.profile_feature.domain.repository.RemoteProfileRepo

class UpdateUserData (
    private val remoteProfileRepo: RemoteProfileRepo
) {

    operator fun invoke(
        newData: Map<String, Any?>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ){
        remoteProfileRepo.updateUserData(
            newData = newData,
            onSuccess = {onSuccess()},
            onFailure = {e -> onFailure(e)}
        )
    }
}