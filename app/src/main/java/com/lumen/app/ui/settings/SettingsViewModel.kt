package com.lumen.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.app.core.locale.LocaleManager
import com.lumen.app.core.theme.LumenAppSettings
import com.lumen.app.core.theme.LumenThemeOption
import com.lumen.app.core.theme.ThemeManager
import com.lumen.app.core.util.LumenStorageManager
import com.lumen.app.domain.model.StorageBreakdown
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeManager: ThemeManager,
    private val localeManager: LocaleManager,
    private val storageManager: LumenStorageManager,
) : ViewModel() {

    val settings: StateFlow<LumenAppSettings> = themeManager.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LumenAppSettings())

    fun setTheme(option: LumenThemeOption) = viewModelScope.launch { themeManager.setTheme(option) }
    fun setForceDark(enabled: Boolean) = viewModelScope.launch { themeManager.setForceDark(enabled) }

    fun setLanguage(tag: String) = viewModelScope.launch {
        themeManager.setLanguage(tag)
        localeManager.applyLanguage(tag)
    }

    fun setLiveOcrEnabled(enabled: Boolean) = viewModelScope.launch { themeManager.setLiveOcrEnabled(enabled) }
    fun setCloudAiEnabled(enabled: Boolean) = viewModelScope.launch { themeManager.setCloudAiEnabled(enabled) }
    fun setBiometricLockEnabled(enabled: Boolean) = viewModelScope.launch { themeManager.setBiometricLockEnabled(enabled) }

    fun computeStorageBreakdown(): StorageBreakdown = storageManager.computeBreakdown()
    fun clearCache(): Long = storageManager.clearCache()
}
