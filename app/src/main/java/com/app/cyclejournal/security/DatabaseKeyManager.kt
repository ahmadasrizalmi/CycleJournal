package com.app.cyclejournal.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.util.Base64
import java.nio.ByteBuffer
import java.security.KeyStore
import java.security.SecureRandom
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages the encryption passphrase for SQLCipher using hardware-backed Envelope Encryption.
 *
 * Pattern:
 * 1. A Master Key is created inside AndroidKeyStore (hardware TEE/StrongBox) with AES-256-GCM.
 * 2. A 256-bit (32-byte) cryptographically secure pseudorandom database key is generated via SecureRandom.
 * 3. The 32-byte key is encrypted using the Keystore Master Key and stored in private SharedPreferences.
 * 4. Plaintext key bytes are wiped from memory immediately after SQLCipher initialization.
 */
class DatabaseKeyManager(private val context: Context) {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "cycle_journal_master_key"
        private const val PREFS_NAME = "db_key_secure_storage"
        private const val KEY_ENCRYPTED_DB_PASSPHRASE = "encrypted_db_passphrase"

        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH_BYTES = 12
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val DB_KEY_SIZE_BYTES = 32
    }

    private val secureRandom = SecureRandom()

    /**
     * Retrieves the decrypted 32-byte database passphrase for SQLCipher.
     * Generates a new key if it does not yet exist.
     *
     * Callers MUST call [Arrays.fill(passphrase, 0.toByte())] after passing the bytes
     * to [net.zetetic.database.sqlcipher.SupportFactory] to eliminate plaintext from RAM.
     */
    @Synchronized
    fun getOrCreateDatabasePassphrase(): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encryptedBase64 = prefs.getString(KEY_ENCRYPTED_DB_PASSPHRASE, null)

        return if (encryptedBase64.isNullOrEmpty()) {
            val newRawKey = ByteArray(DB_KEY_SIZE_BYTES).also { secureRandom.nextBytes(it) }
            val masterKey = getOrCreateMasterKey()

            // Encrypt new key with Keystore master key
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, masterKey)
            val iv = cipher.iv
            val ciphertext = cipher.doFinal(newRawKey)

            // Pack [IV (12B)] + [Ciphertext + Tag]
            val buffer = ByteBuffer.allocate(iv.size + ciphertext.size)
            buffer.put(iv)
            buffer.put(ciphertext)
            val packedBase64 = Base64.getEncoder().encodeToString(buffer.array())

            prefs.edit().putString(KEY_ENCRYPTED_DB_PASSPHRASE, packedBase64).apply()
            newRawKey
        } else {
            val packedBytes = Base64.getDecoder().decode(encryptedBase64)
            val buffer = ByteBuffer.wrap(packedBytes)

            val iv = ByteArray(GCM_IV_LENGTH_BYTES)
            buffer.get(iv)
            val ciphertext = ByteArray(buffer.remaining())
            buffer.get(ciphertext)

            val masterKey = getOrCreateMasterKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, masterKey, spec)

            cipher.doFinal(ciphertext)
        }
    }

    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            val entry = keyStore.getEntry(MASTER_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (entry != null) return entry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keyGenSpec = KeyGenParameterSpec.Builder(
            MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false) // Allows background workers to access DB
            .build()

        keyGenerator.init(keyGenSpec)
        return keyGenerator.generateKey()
    }

    /**
     * Permanently destroys the database key and Keystore entries as part of the GDPR Nuke Option.
     */
    @Synchronized
    fun destroyDatabaseKey() {
        // 1. Wipe SharedPreferences
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit()

        // 2. Delete Keystore Master Key entry
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (keyStore.containsAlias(MASTER_KEY_ALIAS)) {
                keyStore.deleteEntry(MASTER_KEY_ALIAS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
