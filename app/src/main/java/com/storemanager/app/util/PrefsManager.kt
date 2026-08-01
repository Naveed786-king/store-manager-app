package com.storemanager.app.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "store_manager_prefs")

object PrefsKeys {
    val PIN = stringPreferencesKey("pin_code")
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val ADMIN_NAME = stringPreferencesKey("admin_name")
    val IS_SETUP = booleanPreferencesKey("is_setup")
}

class PrefsManager(private val context: Context) {
    val pin: Flow<String?> = context.dataStore.data.map { it[PrefsKeys.PIN] }
    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[PrefsKeys.DARK_MODE] ?: false }
    val isSetup: Flow<Boolean> = context.dataStore.data.map { it[PrefsKeys.IS_SETUP] ?: false }
    val adminName: Flow<String?> = context.dataStore.data.map { it[PrefsKeys.ADMIN_NAME] }

    suspend fun setPin(pin: String) {
        context.dataStore.edit {
            it[PrefsKeys.PIN] = pin
            it[PrefsKeys.IS_SETUP] = true
        }
    }

    suspend fun setAdminName(name: String) {
        context.dataStore.edit { it[PrefsKeys.ADMIN_NAME] = name }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[PrefsKeys.DARK_MODE] = enabled }
    }
}
