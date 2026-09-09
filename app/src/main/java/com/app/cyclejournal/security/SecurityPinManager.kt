package com.app.cyclejournal.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.util.UUID

/**
 * Secure manager for user PIN hash and Anonymous Patient ID backed by EncryptedSharedPreferences.
 */
class SecurityPinManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "secure_user_pin_prefs"
        private const val KEY_PIN_HASH = "user_pin_sha256"
        private const val KEY_ANONYMOUS_USER_ID = "anonymous_patient_id"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun isPinSet(): Boolean {
        return !securePrefs.getString(KEY_PIN_HASH, null).isNullOrEmpty()
    }

    fun savePin(pin: String) {
        val hash = hashPin(pin)
        securePrefs.edit().putString(KEY_PIN_HASH, hash).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = securePrefs.getString(KEY_PIN_HASH, null) ?: return false
        return storedHash == hashPin(pin)
    }

    fun getOrCreateAnonymousUserId(): String {
        var id = securePrefs.getString(KEY_ANONYMOUS_USER_ID, null)
        if (id.isNullOrEmpty()) {
            val uuidPart = UUID.randomUUID().toString().replace("-", "").take(12)
            id = "px-$uuidPart"
            securePrefs.edit().putString(KEY_ANONYMOUS_USER_ID, id).apply()
        }
        return id
    }

    private fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(pin.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun clear() {
        securePrefs.edit().clear().commit()
    }
}
