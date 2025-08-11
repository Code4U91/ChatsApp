package com.example.chatapp.profile_feature.domain.use_case

import com.example.chatapp.profile_feature.domain.repository.RemoteProfileRepo

class UpdateStoredEmail (
    private val remoteProfileRepo: RemoteProfileRepo
) {
    operator fun invoke(currentEmail : String){
        remoteProfileRepo.checkAndUpdateEmailOnFireStore(

            currentEmailInDb =  currentEmail
        )
    }
}