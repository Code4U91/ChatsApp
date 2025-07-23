package com.example.chatapp.auth_feature.domain.usecase.auth_case

import com.example.chatapp.auth_feature.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser

class GetCurrentUser (
    private val authRepository: AuthRepository
) {
    operator fun invoke(): FirebaseUser? {

        return authRepository.getCurrentUser()
    }
}