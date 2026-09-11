package com.lumen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.lumen.app.core.locale.LocaleManager
import com.lumen.app.core.navigation.LumenNavHost
import com.lumen.app.core.theme.LumenTheme
import com.lumen.app.core.theme.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var themeManager: ThemeManager
    @Inject lateinit var localeManager: LocaleManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by themeManager.settingsFlow.collectAsState(initial = null)
            val currentSettings = settings ?: return@setContent

            val isRtl = localeManager.isRtl(currentSettings.languageTag)
            val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                LumenTheme(
                    themeOption = currentSettings.theme,
                    useSystemDarkVariant = currentSettings.forceDark || androidx.compose.foundation.isSystemInDarkTheme(),
                ) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        LumenNavHost()
                    }
                }
            }
        }
    }

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(newBase)
    }
}
