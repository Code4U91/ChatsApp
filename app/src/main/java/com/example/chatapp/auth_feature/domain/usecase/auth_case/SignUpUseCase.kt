package com.example.chatapp.auth_feature.domain.usecase.auth_case

import com.example.chatapp.auth_feature.domain.repository.AuthRepository

class SignUpUseCase(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        email: String,
        password: String,
        userName: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ){

        authRepository.signUpUsingEmailAndPwd(
            email =  email,
            password = password,
            userName = userName,
            onSuccess = {onSuccess},
            onFailure = {e -> onFailure(e)}
        )
    }
}