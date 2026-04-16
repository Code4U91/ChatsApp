package com.code4u.chatsapp.profile_feature.domain.use_case

import com.code4u.chatsapp.profile_feature.domain.repository.RemoteProfileRepo

class UpdateStoredEmail (
    private val remoteProfileRepo: RemoteProfileRepo
) {
    operator fun invoke(currentEmail : String){
        remoteProfileRepo.checkAndUpdateEmailOnFireStore(

            currentEmailInDb =  currentEmail
        )
    }
}