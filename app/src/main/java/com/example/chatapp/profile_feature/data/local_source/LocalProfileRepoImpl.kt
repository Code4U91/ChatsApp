package com.example.chatapp.profile_feature.data.local_source

import com.example.chatapp.profile_feature.data.remote_source.UserData
import com.example.chatapp.profile_feature.data.remote_source.toEntity
import com.example.chatapp.profile_feature.domain.model.CurrentUser
import com.example.chatapp.profile_feature.domain.repository.LocalProfileRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalProfileRepoImpl (
    private val userDao: UserDao
) : LocalProfileRepo {

    override fun getUserData(): Flow<CurrentUser?> {
        return userDao.getUserData().map { entity -> entity?.toDomain()  }
    }

    override suspend fun insertUserData(user: UserData) {

        userDao.insertUser(user.toEntity())
    }

}