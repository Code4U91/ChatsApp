package com.example.chatapp.auth_feature.domain.usecase.auth_case

import com.example.chatapp.auth_feature.domain.repository.AuthRepository

class SignOutUseCase (
     private val authRepository: AuthRepository
) {

    suspend operator fun invoke(){

        authRepository.signOut()
    }
}