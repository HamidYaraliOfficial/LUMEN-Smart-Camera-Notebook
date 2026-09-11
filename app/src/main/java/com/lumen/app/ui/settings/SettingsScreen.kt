package com.lumen.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumen.app.core.locale.LocaleManager
import com.lumen.app.core.theme.LumenThemeOption
import com.lumen.app.ui.components.LumenCard
import com.lumen.app.ui.components.LumenTopBar

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenBusinessHours: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(topBar = { LumenTopBar(title = "Settings", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LumenCard {
                Text("Theme", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                ThemeOptionRow("Windows 11 (default)", settings.theme == LumenThemeOption.WINDOWS11) { viewModel.setTheme(LumenThemeOption.WINDOWS11) }
                ThemeOptionRow("Light", settings.theme == LumenThemeOption.LIGHT) { viewModel.setTheme(LumenThemeOption.LIGHT) }
                ThemeOptionRow("Dark", settings.theme == LumenThemeOption.DARK) { viewModel.setTheme(LumenThemeOption.DARK) }
                ThemeOptionRow("Red", settings.theme == LumenThemeOption.RED) { viewModel.setTheme(LumenThemeOption.RED) }
                ThemeOptionRow("Blue", settings.theme == LumenThemeOption.BLUE) { viewModel.setTheme(LumenThemeOption.BLUE) }
            }

            LumenCard {
                Text("Language / زبان / 语言", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                LocaleManager.SUPPORTED_LANGUAGES.forEach { tag ->
                    LanguageOptionRow(LocaleManager.displayNameFor(tag), settings.languageTag == tag) { viewModel.setLanguage(tag) }
                }
            }

            LumenCard {
                Text("Capture & AI", fontWeight = FontWeight.SemiBold)
                SwitchRow("Live OCR overlay", settings.liveOcrEnabled, viewModel::setLiveOcrEnabled)
                SwitchRow("Enable optional Cloud AI", settings.cloudAiEnabled, viewModel::setCloudAiEnabled)
                SwitchRow("Biometric lock for private notebooks", settings.biometricLockEnabled, viewModel::setBiometricLockEnabled)
            }

            LumenCard {
                Text("Business hours", fontWeight = FontWeight.SemiBold)
                Text("Set up open/closed hours and LUMEN will show live status and time to next change.", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onOpenBusinessHours) { Text("Manage business hours") }
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label)
    }
}

@Composable
private fun LanguageOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label)
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
