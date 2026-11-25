package com.example.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "motoopaint_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val WORKSHOP_NAME = stringPreferencesKey("workshop_name")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.SYSTEM)
    }

    val workshopName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.WORKSHOP_NAME] ?: "Nairobi Paint Bay"
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setWorkshopName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WORKSHOP_NAME] = name.trim().ifEmpty { "Nairobi Paint Bay" }
        }
    }
}
