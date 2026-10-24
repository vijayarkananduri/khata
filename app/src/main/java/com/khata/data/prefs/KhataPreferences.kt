package com.khata.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.khataStore by preferencesDataStore(name = "khata_preferences")

data class UserSettings(
    val username: String = "",
    val language: String = "te",
    val romanScript: Boolean = false,
    val themeMode: String = "SYSTEM",
    val reduceMotion: String = "FOLLOW_SYSTEM",
    val hapticsEnabled: Boolean = true,
    val onboardingComplete: Boolean = false,
    val monthlyBudgetPaise: Long = 0L,
    val compareMode: Boolean = true,
    val nudgesEnabled: Boolean = true
)

@Singleton
class KhataPreferences @Inject constructor(private val store: DataStore<Preferences>, @ApplicationContext private val context: Context) {
    private object Keys {
        val username = stringPreferencesKey("username")
        val language = stringPreferencesKey("language")
        val roman = booleanPreferencesKey("roman_script")
        val theme = stringPreferencesKey("theme_mode")
        val motion = stringPreferencesKey("reduce_motion")
        val haptics = booleanPreferencesKey("haptics_enabled")
        val onboarding = booleanPreferencesKey("onboarding_complete")
        val budget = longPreferencesKey("monthly_budget_paise")
        val compare = booleanPreferencesKey("compare_mode")
        val nudges = booleanPreferencesKey("nudges_enabled")
    }

    val settings: Flow<UserSettings> = store.data.catch { error ->
        if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw error
    }.map { p ->
        UserSettings(
            username = p[Keys.username] ?: "", language = p[Keys.language] ?: "te",
            romanScript = p[Keys.roman] ?: false, themeMode = p[Keys.theme] ?: "SYSTEM",
            reduceMotion = p[Keys.motion] ?: "FOLLOW_SYSTEM",
            hapticsEnabled = p[Keys.haptics] ?: true, onboardingComplete = p[Keys.onboarding] ?: false,
            monthlyBudgetPaise = p[Keys.budget] ?: 0L, compareMode = p[Keys.compare] ?: true,
            nudgesEnabled = p[Keys.nudges] ?: true
        )
    }

    suspend fun completeOnboarding(username: String, language: String, roman: Boolean, budgetPaise: Long) {
        store.edit { p ->
            p[Keys.username] = username.trim()
            p[Keys.language] = language
            p[Keys.roman] = roman
            p[Keys.budget] = budgetPaise
            p[Keys.compare] = budgetPaise <= 0L
            p[Keys.onboarding] = true
        }
    }
    suspend fun setUsername(value: String) = store.edit { it[Keys.username] = value }
    suspend fun setLanguage(value: String) = store.edit { it[Keys.language] = value }
    suspend fun setRoman(value: Boolean) = store.edit { it[Keys.roman] = value }
    suspend fun setTheme(value: String) = store.edit { it[Keys.theme] = value }
    suspend fun setMotion(value: String) = store.edit { it[Keys.motion] = value }
    suspend fun setHaptics(value: Boolean) = store.edit { it[Keys.haptics] = value }
    suspend fun setBudget(value: Long) = store.edit { it[Keys.budget] = value }
    suspend fun setCompare(value: Boolean) = store.edit { it[Keys.compare] = value }
    suspend fun setNudges(value: Boolean) {
        store.edit { it[Keys.nudges] = value }
        context.getSharedPreferences("khata_nudge_worker", Context.MODE_PRIVATE).edit().putBoolean("enabled", value).apply()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {
    @Provides @Singleton
    fun providePreferences(@ApplicationContext context: Context): DataStore<Preferences> = context.khataStore
}
