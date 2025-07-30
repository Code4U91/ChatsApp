package com.example.chatapp.profile_feature.domain.repository

import com.example.chatapp.profile_feature.data.remote_source.UserData
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface RemoteProfileRepo {

    fun fetchUserData(user: FirebaseUser) : Flow<UserData?>

    fun clearUserDataListener()
}