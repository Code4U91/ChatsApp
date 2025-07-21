package com.example.chatapp.auth_feature.domain.usecase.auth_case

import android.app.Activity
import com.example.chatapp.auth_feature.domain.repository.AuthRepository

class SignInUseCase (
    private val authRepository: AuthRepository
) {
    
    suspend operator fun invoke(
        activity: Activity, 
        withGoogle : Boolean,
        email : String,
        password : String,
        onSuccess : () -> Unit,
        onFailure: (Exception) -> Unit){
        
        if(withGoogle){

            val token = authRepository.signInWithGoogle(activity)?.idToken
            authRepository.fireBaseAuthWithGoogle(
                token,
                onSuccess = {onSuccess},
                onFailure = {e -> onFailure(e) }
            )

        } else {
            authRepository.signInUsingEmailAndPwd(
                email, password,
                onSuccess = { onSuccess },
                onFailure =  { e -> onFailure(e) }
            )
        }
        
    }
}