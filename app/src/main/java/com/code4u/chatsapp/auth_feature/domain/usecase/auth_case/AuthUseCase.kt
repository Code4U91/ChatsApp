package com.code4u.chatsapp.auth_feature.domain.usecase.auth_case

data class AuthUseCase(
    val signInUseCase: SignInUseCase,
    val signOutUseCase: SignOutUseCase,
    val changeEmailUseCase: ChangeEmailUseCase,
    val resetPasswordUseCase: ResetPasswordUseCase,
    val getCurrentUser: GetCurrentUser,
    val signUpUseCase: SignUpUseCase,
    val updateFcmTokenIfNeeded: UpdateFcmTokenIfNeeded
)