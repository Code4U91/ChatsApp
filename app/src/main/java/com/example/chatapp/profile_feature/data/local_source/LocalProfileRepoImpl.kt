package com.example.chatapp.profile_feature.data.local_source

import com.example.chatapp.core.local_database.LocalRoomDatabase
import com.example.chatapp.profile_feature.data.remote_source.UserData
import com.example.chatapp.profile_feature.data.remote_source.toEntity
import com.example.chatapp.profile_feature.domain.model.CurrentUser
import com.example.chatapp.profile_feature.domain.repository.LocalProfileRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalProfileRepoImpl (
    private val userDao: UserDao,
    private val db : LocalRoomDatabase
) : LocalProfileRepo {

    override fun getUserData(): Flow<CurrentUser?> {
        return userDao.getUserData().map { entity -> entity?.toDomain()  }
    }

    override suspend fun insertUserData(user: UserData) {

        userDao.insertUser(user.toEntity())
    }

    override suspend fun clearAllDbTables() {

        withContext(Dispatchers.IO){
            db.clearAllTables()
        }

    }

}