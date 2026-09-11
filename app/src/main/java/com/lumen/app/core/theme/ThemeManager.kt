package com.lumen.app.core.theme

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "lumen_settings")

data class LumenAppSettings(
    val theme: LumenThemeOption = LumenThemeOption.WINDOWS11,
    val forceDark: Boolean = false,
    val languageTag: String = "en", // en | fa | zh
    val liveOcrEnabled: Boolean = true,
    val cloudAiEnabled: Boolean = false,
    val biometricLockEnabled: Boolean = false,
)

/**
 * Persists user-facing app preferences (theme, language, feature toggles) with DataStore.
 * This is intentionally separate from [com.lumen.app.data.local.entity.BusinessHoursEntity],
 * which stores structured per-day operating-hours data in Room.
 */
@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme_option")
        val FORCE_DARK = booleanPreferencesKey("force_dark")
        val LANGUAGE = stringPreferencesKey("language_tag")
        val LIVE_OCR = booleanPreferencesKey("live_ocr_enabled")
        val CLOUD_AI = booleanPreferencesKey("cloud_ai_enabled")
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock_enabled")
    }

    val settingsFlow: Flow<LumenAppSettings> = appContext.settingsDataStore.data.map { prefs ->
        LumenAppSettings(
            theme = LumenThemeOption.fromId(prefs[Keys.THEME]),
            forceDark = prefs[Keys.FORCE_DARK] ?: false,
            languageTag = prefs[Keys.LANGUAGE] ?: "en",
            liveOcrEnabled = prefs[Keys.LIVE_OCR] ?: true,
            cloudAiEnabled = prefs[Keys.CLOUD_AI] ?: false,
            biometricLockEnabled = prefs[Keys.BIOMETRIC_LOCK] ?: false,
        )
    }

    suspend fun setTheme(option: LumenThemeOption) {
        appContext.settingsDataStore.edit { it[Keys.THEME] = option.id }
    }

    suspend fun setForceDark(enabled: Boolean) {
        appContext.settingsDataStore.edit { it[Keys.FORCE_DARK] = enabled }
    }

    suspend fun setLanguage(tag: String) {
        appContext.settingsDataStore.edit { it[Keys.LANGUAGE] = tag }
    }

    suspend fun setLiveOcrEnabled(enabled: Boolean) {
        appContext.settingsDataStore.edit { it[Keys.LIVE_OCR] = enabled }
    }

    suspend fun setCloudAiEnabled(enabled: Boolean) {
        appContext.settingsDataStore.edit { it[Keys.CLOUD_AI] = enabled }
    }

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        appContext.settingsDataStore.edit { it[Keys.BIOMETRIC_LOCK] = enabled }
    }
}
