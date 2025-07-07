package com.example.chatapp.localData.dataStore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first


private val Context.dataStore by preferencesDataStore(name = "fcm_prefs")

// used for storing current device fcm token and later removing it from database
// and local memory when user logs out
object LocalFcmTokenManager{

    private val TOKEN_KEY = stringPreferencesKey("fcm_token")

    suspend fun saveToken(context: Context, token: String){
            context.dataStore.edit { prefs ->
                prefs[TOKEN_KEY] = token
            }

    }

    suspend fun getToken(context: Context): String?{
        val prefs = context.dataStore.data.first()
        return prefs[TOKEN_KEY]
    }

    suspend fun clearToken(context: Context){
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }
}