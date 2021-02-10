package com.example.musicapplication.Model

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.createDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

const val PREFERENCE_NAME = "preference"

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private object PreferenceKey{
        val name = stringPreferencesKey("songPath")
    }

    private val dataStore = context.createDataStore(name = PREFERENCE_NAME)

    suspend fun saveData(path: String) {
        dataStore.edit {
            it[PreferenceKey.name] = path
        }
    }

    val readData = dataStore.data.catch { exception ->
        throw exception
    }.map {
        val path = it[PreferenceKey.name] ?: "non"
        path
    }

}