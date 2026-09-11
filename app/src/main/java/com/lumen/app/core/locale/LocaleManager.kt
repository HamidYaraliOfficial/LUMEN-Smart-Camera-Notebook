package com.lumen.app.core.locale

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LUMEN supports English (LTR), Persian/Farsi (RTL) and Chinese Simplified (LTR).
 * On Android 13+ we use the platform per-app language API; below that we fall back
 * to AppCompatDelegate's compat implementation, which also works pre-13.
 */
@Singleton
class LocaleManager @Inject constructor() {

    fun applyLanguage(tag: String) {
        val locales = LocaleListCompat.forLanguageTags(tag)
        AppCompatDelegate.setApplicationLocales(locales)
    }

    fun currentLanguageTag(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (!locales.isEmpty) locales[0]?.toLanguageTag()?.substringBefore("-") ?: "en" else Locale.getDefault().language
    }

    fun isRtl(tag: String): Boolean = tag == "fa" || tag == "ar" || tag == "he" || tag == "ur"

    companion object {
        val SUPPORTED_LANGUAGES = listOf("en", "fa", "zh")

        fun displayNameFor(tag: String): String = when (tag) {
            "en" -> "English"
            "fa" -> "فارسی"
            "zh" -> "中文"
            else -> tag
        }
    }
}
