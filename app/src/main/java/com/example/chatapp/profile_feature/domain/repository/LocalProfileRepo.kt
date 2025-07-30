package com.example.chatapp.profile_feature.domain.repository

import com.example.chatapp.profile_feature.data.remote_source.UserData
import com.example.chatapp.profile_feature.domain.model.CurrentUser
import kotlinx.coroutines.flow.Flow

interface LocalProfileRepo {

    fun getUserData() : Flow<CurrentUser?>

    suspend fun insertUserData(user : UserData)
}