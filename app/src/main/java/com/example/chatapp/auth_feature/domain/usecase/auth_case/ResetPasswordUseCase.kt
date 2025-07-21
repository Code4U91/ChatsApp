package com.example.chatapp.auth_feature.domain.usecase.auth_case

import com.example.chatapp.auth_feature.domain.repository.AuthRepository

class ResetPasswordUseCase (
    private val authRepository: AuthRepository
) {

    operator fun invoke(email : String){

        authRepository.resetPassword(email)
    }

}