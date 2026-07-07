package com.restaurant.offlinemanager.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

data class AppSettings(
    val darkMode: Boolean = true,
    val currency: String = "تومان",
    val language: String = "fa",
    val appLockEnabled: Boolean = false,
    val lowStockNotificationsEnabled: Boolean = true,
    val defaultWarehouseId: Long? = null
)

class AppSettingsRepository(private val context: Context) {
    private object Keys {
        val DarkMode = booleanPreferencesKey("dark_mode")
        val Currency = stringPreferencesKey("currency")
        val Language = stringPreferencesKey("language")
        val AppLockEnabled = booleanPreferencesKey("app_lock_enabled")
        val LowStockNotificationsEnabled = booleanPreferencesKey("low_stock_notifications_enabled")
        val DefaultWarehouseId = longPreferencesKey("default_warehouse_id")
    }

    val settings: Flow<AppSettings> = context.appSettingsDataStore.data
        .catch { error ->
            if (error is IOException) emit(preferencesOf()) else throw error
        }
        .map { preferences ->
            AppSettings(
                darkMode = preferences[Keys.DarkMode] ?: true,
                currency = preferences[Keys.Currency] ?: "تومان",
                language = preferences[Keys.Language] ?: "fa",
                appLockEnabled = preferences[Keys.AppLockEnabled] ?: false,
                lowStockNotificationsEnabled = preferences[Keys.LowStockNotificationsEnabled] ?: true,
                defaultWarehouseId = preferences[Keys.DefaultWarehouseId]
            )
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.DarkMode] = enabled }
    }

    suspend fun setAppLock(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.AppLockEnabled] = enabled }
    }

    suspend fun setLowStockNotifications(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.LowStockNotificationsEnabled] = enabled }
    }

    suspend fun setDefaultWarehouse(id: Long?) {
        context.appSettingsDataStore.edit { preferences ->
            if (id == null) preferences.remove(Keys.DefaultWarehouseId) else preferences[Keys.DefaultWarehouseId] = id
        }
    }
}
