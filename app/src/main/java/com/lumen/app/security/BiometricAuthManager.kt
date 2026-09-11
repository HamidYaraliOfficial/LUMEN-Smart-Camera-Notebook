package com.lumen.app.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

sealed class BiometricResult {
    data object Success : BiometricResult()
    data object NotAvailable : BiometricResult()
    data class Failed(val message: String) : BiometricResult()
    data object Cancelled : BiometricResult()
}

/** Real BiometricPrompt integration used to gate access to locked notebooks/documents. */
@Singleton
class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val appContext: android.content.Context
) {
    fun isAvailable(): Boolean {
        val manager = BiometricManager.from(appContext)
        return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    suspend fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
    ): BiometricResult = suspendCancellableCoroutine { cont ->
        if (!isAvailable()) {
            cont.resume(BiometricResult.NotAvailable)
            return@suspendCancellableCoroutine
        }
        val executor = ContextCompat.getMainExecutor(appContext)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                if (cont.isActive) cont.resume(BiometricResult.Success)
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (!cont.isActive) return
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    cont.resume(BiometricResult.Cancelled)
                } else {
                    cont.resume(BiometricResult.Failed(errString.toString()))
                }
            }
            override fun onAuthenticationFailed() {
                // A single failed attempt (bad fingerprint read etc.) — prompt stays open, so we don't resume here.
            }
        }
        val prompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
        prompt.authenticate(promptInfo)
        cont.invokeOnCancellation { }
    }
}
