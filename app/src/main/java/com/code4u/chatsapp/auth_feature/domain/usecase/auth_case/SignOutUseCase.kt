package com.code4u.chatsapp.auth_feature.domain.usecase.auth_case

import com.code4u.chatsapp.auth_feature.domain.repository.AuthRepository

class SignOutUseCase (
     private val authRepository: AuthRepository
) {

    suspend operator fun invoke(){

        authRepository.signOut()
    }
}