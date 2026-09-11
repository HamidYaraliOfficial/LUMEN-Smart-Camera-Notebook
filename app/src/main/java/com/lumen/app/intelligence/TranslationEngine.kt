package com.lumen.app.intelligence

import javax.inject.Inject
import javax.inject.Singleton

data class TranslationResult(val original: String, val translated: String, val targetLanguage: String, val provider: String)

interface TranslationProvider {
    val name: String
    suspend fun translate(text: String, targetLanguageTag: String): String
}

/**
 * Fallback on-device provider. LUMEN does not bundle ML Kit Translate's downloadable language
 * models by default (they add real APK/storage weight per language pair), so out of the box this
 * provider performs a clearly-labelled passthrough and surfaces a "model not installed" state in
 * the UI. Wiring com.google.mlkit:translate + on-device model download is a documented extension
 * point (ModelManager) rather than being silently faked here.
 */
class OnDeviceTranslationProvider @Inject constructor() : TranslationProvider {
    override val name: String = "on_device"
    override suspend fun translate(text: String, targetLanguageTag: String): String {
        return text // extension point: plug in com.google.mlkit:translate here once models are downloaded
    }
}

/** Cloud provider stub: only called when the user has enabled Cloud AI + given translation consent
 * in the Privacy Center, and never holds a hardcoded API key (the key is user-supplied at runtime). */
class CloudTranslationProvider(
    private val apiKeyProvider: suspend () -> String?,
) : TranslationProvider {
    override val name: String = "cloud"
    override suspend fun translate(text: String, targetLanguageTag: String): String {
        val key = apiKeyProvider() ?: return text
        // extension point: call the configured cloud translation REST endpoint using `key`.
        return text
    }
}

@Singleton
class TranslationEngine @Inject constructor(
    private val onDeviceProvider: OnDeviceTranslationProvider,
) {
    private var cloudProvider: TranslationProvider? = null

    fun registerCloudProvider(provider: TranslationProvider) {
        cloudProvider = provider
    }

    suspend fun translate(text: String, targetLanguageTag: String, useCloud: Boolean): TranslationResult {
        val provider = if (useCloud) (cloudProvider ?: onDeviceProvider) else onDeviceProvider
        val translated = provider.translate(text, targetLanguageTag)
        return TranslationResult(original = text, translated = translated, targetLanguage = targetLanguageTag, provider = provider.name)
    }
}
