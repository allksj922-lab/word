package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.DifficultyLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wordchain_prefs")

data class UserPreferences(
    val selectedDifficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
    val timerEnabled: Boolean = true,
    val strictLevelMode: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val SELECTED_DIFFICULTY = stringPreferencesKey("selected_difficulty")
        val TIMER_ENABLED = booleanPreferencesKey("timer_enabled")
        val STRICT_LEVEL_MODE = booleanPreferencesKey("strict_level_mode")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            selectedDifficulty = DifficultyLevel.fromName(prefs[Keys.SELECTED_DIFFICULTY]),
            timerEnabled = prefs[Keys.TIMER_ENABLED] ?: true,
            strictLevelMode = prefs[Keys.STRICT_LEVEL_MODE] ?: false
        )
    }

    suspend fun setSelectedDifficulty(level: DifficultyLevel) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_DIFFICULTY] = level.name
        }
    }

    suspend fun setTimerEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TIMER_ENABLED] = enabled
        }
    }

    suspend fun setStrictLevelMode(strict: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STRICT_LEVEL_MODE] = strict
        }
    }
}
