package com.example.chatapp.profile_feature.domain.repository

import com.example.chatapp.profile_feature.data.remote_source.UserData
import kotlinx.coroutines.flow.Flow

interface RemoteProfileRepo {

    fun fetchUserData() : Flow<UserData?>

    fun clearProfileUserDataListener()

    suspend fun oneTimeUserDataFetch() : UserData?


}