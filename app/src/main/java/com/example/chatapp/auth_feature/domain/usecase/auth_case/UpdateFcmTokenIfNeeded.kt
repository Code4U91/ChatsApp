package com.example.chatapp.auth_feature.domain.usecase.auth_case

import com.example.chatapp.auth_feature.domain.repository.AuthRepository

class UpdateFcmTokenIfNeeded (
    private val authRepository: AuthRepository
){
    suspend operator fun invoke(savedTokens : List<String>) {

        authRepository.updateFcmTokenIfNeeded(savedTokens)
    }
}