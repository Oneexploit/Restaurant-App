package com.restaurant.offlinemanager.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

data class AppSettings(
    val lowStockNotificationsEnabled: Boolean = true,
    val reducedMotionEnabled: Boolean = false
)

class AppSettingsRepository(private val context: Context) {
    private object Keys {
        val LowStockNotificationsEnabled = booleanPreferencesKey("low_stock_notifications_enabled")
        val ReducedMotionEnabled = booleanPreferencesKey("reduced_motion_enabled")
    }

    val settings: Flow<AppSettings> = context.appSettingsDataStore.data
        .catch { error ->
            if (error is IOException) emit(preferencesOf()) else throw error
        }
        .map { preferences ->
            AppSettings(
                lowStockNotificationsEnabled = preferences[Keys.LowStockNotificationsEnabled] ?: true,
                reducedMotionEnabled = preferences[Keys.ReducedMotionEnabled] ?: false
            )
        }

    suspend fun setLowStockNotifications(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.LowStockNotificationsEnabled] = enabled }
    }

    suspend fun setReducedMotion(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.ReducedMotionEnabled] = enabled }
    }
}
