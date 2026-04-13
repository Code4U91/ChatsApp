package com.code4u.chatsapp.auth_feature.domain.usecase.auth_case

import com.code4u.chatsapp.auth_feature.domain.repository.AuthRepository

class ResetPasswordUseCase(
    private val authRepository: AuthRepository
) {

    operator fun invoke(email: String): String {

        return authRepository.resetPassword(email)
    }

}