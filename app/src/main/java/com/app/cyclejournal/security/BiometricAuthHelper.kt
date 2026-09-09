package com.app.cyclejournal.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Helper for hardware biometric authentication (Fingerprint / Face Unlock) with fallback to PIN.
 */
class BiometricAuthHelper(private val activity: FragmentActivity) {

    fun canAuthenticate(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun promptBiometric(
        title: String = "Buka Kunci CycleJournal",
        subtitle: String = "Verifikasi identitas Anda untuk mengakses data klinis",
        onSuccess: () -> Unit,
        onErrorOrFallback: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Gunakan PIN")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onErrorOrFallback()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Transient biometric sensor misread, user can retry
            }
        })

        biometricPrompt.authenticate(promptInfo)
    }
}
