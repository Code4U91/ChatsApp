package com.example.chatapp.profile_feature.domain.repository

import com.example.chatapp.profile_feature.data.remote_source.UserData
import kotlinx.coroutines.flow.Flow

interface RemoteProfileRepo {

    fun fetchUserData(): Flow<UserData?>

    suspend fun oneTimeUserDataFetch(): UserData?

    fun updateUserData(
        newData: Map<String, Any?>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun checkAndUpdateEmailOnFireStore(
        currentEmailInDb: String,
    )


}