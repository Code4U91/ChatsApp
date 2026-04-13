package com.code4u.chatsapp.profile_feature.domain.repository

import com.code4u.chatsapp.profile_feature.data.remote_source.UserData
import com.code4u.chatsapp.profile_feature.domain.model.CurrentUser
import kotlinx.coroutines.flow.Flow

interface LocalProfileRepo {

    fun getUserData() : Flow<CurrentUser?>

    suspend fun insertUserData(user : UserData)

     suspend fun clearAllDbTables()
}