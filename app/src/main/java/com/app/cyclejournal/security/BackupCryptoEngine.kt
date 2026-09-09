package com.app.cyclejournal.security

import java.util.Base64
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class EncryptedBackupPackage(
    val base64Payload: String,
    val sha256Checksum: String
)

/**
 * Zero-Knowledge cryptographic engine for on-device backup encryption and cloud payload verification.
 * Employs AES-256-GCM authenticated encryption paired with PBKDF2 key derivation (100,000 iterations).
 */
class BackupCryptoEngine {

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATION_COUNT = 100_000
        private const val KEY_LENGTH_BITS = 256
        private const val SALT_LENGTH_BYTES = 16
        private const val IV_LENGTH_BYTES = 12 // NIST recommended GCM IV length
        private const val TAG_LENGTH_BITS = 128
    }

    private val secureRandom = SecureRandom()

    /**
     * Encrypts raw JSON payload using the user's PIN/passphrase.
     */
    fun encrypt(rawJson: String, userPassphrase: CharArray): EncryptedBackupPackage {
        // 1. Generate random salt
        val salt = ByteArray(SALT_LENGTH_BYTES).also { secureRandom.nextBytes(it) }

        // 2. Derive AES-256 key via PBKDF2
        val keySpec = PBEKeySpec(userPassphrase, salt, ITERATION_COUNT, KEY_LENGTH_BITS)
        val keyFactory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val keyBytes = keyFactory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        // 3. Generate random 12-byte IV
        val iv = ByteArray(IV_LENGTH_BYTES).also { secureRandom.nextBytes(it) }

        // 4. Encrypt with AES-GCM
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val gcmSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        val ciphertextWithTag = cipher.doFinal(rawJson.toByteArray(Charsets.UTF_8))

        // 5. Binary pack: [Salt (16B)] + [IV (12B)] + [Ciphertext + Tag]
        val combinedBuffer = ByteBuffer.allocate(salt.size + iv.size + ciphertextWithTag.size)
        combinedBuffer.put(salt)
        combinedBuffer.put(iv)
        combinedBuffer.put(ciphertextWithTag)

        val base64Payload = Base64.getEncoder().encodeToString(combinedBuffer.array())
        val checksum = calculateSha256(base64Payload)

        return EncryptedBackupPackage(
            base64Payload = base64Payload,
            sha256Checksum = checksum
        )
    }

    /**
     * Decrypts base64 payload from Cloudflare D1 back to raw JSON.
     * Throws [javax.crypto.AEADBadTagException] if the PIN/passphrase is incorrect.
     */
    fun decrypt(base64Payload: String, userPassphrase: CharArray): String {
        val combinedBytes = Base64.getDecoder().decode(base64Payload)
        val buffer = ByteBuffer.wrap(combinedBytes)

        // 1. Extract Salt
        val salt = ByteArray(SALT_LENGTH_BYTES)
        buffer.get(salt)

        // 2. Extract IV
        val iv = ByteArray(IV_LENGTH_BYTES)
        buffer.get(iv)

        // 3. Extract Ciphertext + Auth Tag
        val ciphertextWithTag = ByteArray(buffer.remaining())
        buffer.get(ciphertextWithTag)

        // 4. Re-derive AES-256 key
        val keySpec = PBEKeySpec(userPassphrase, salt, ITERATION_COUNT, KEY_LENGTH_BITS)
        val keyFactory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val keyBytes = keyFactory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        // 5. Decrypt
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val gcmSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        val decryptedBytes = cipher.doFinal(ciphertextWithTag)

        return String(decryptedBytes, Charsets.UTF_8)
    }

    fun calculateSha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
