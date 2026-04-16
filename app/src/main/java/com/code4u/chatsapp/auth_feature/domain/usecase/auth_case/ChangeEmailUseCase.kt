package com.code4u.chatsapp.auth_feature.domain.usecase.auth_case

import com.code4u.chatsapp.auth_feature.domain.repository.AuthRepository

class ChangeEmailUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(
        email: String, onFailure: (String?) -> Unit,
        onSuccess: () -> Unit) {
        authRepository.updateUserEmail(
            email,
            onFailure = { e -> onFailure(e) },
            onSuccess = { onSuccess() }
        )
    }
}