package com.app.cyclejournal.security

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import javax.crypto.AEADBadTagException

class BackupCryptoEngineTest {

    private lateinit var cryptoEngine: BackupCryptoEngine

    @Before
    fun setUp() {
        cryptoEngine = BackupCryptoEngine()
    }

    @Test
    fun testEncryptionDecryptionRoundtrip() {
        val originalJson = """{"user_id":"px-test1234","daily_logs":[{"date":"2026-09-01","flow":"HEAVY"}]}"""
        val passphrase = "SecretPassword123".toCharArray()

        val encryptedPackage = cryptoEngine.encrypt(originalJson, passphrase)
        assertNotNull(encryptedPackage.base64Payload)
        assertNotNull(encryptedPackage.sha256Checksum)

        // Verify SHA-256 matches payload
        val calculatedChecksum = cryptoEngine.calculateSha256(encryptedPackage.base64Payload)
        assertEquals(calculatedChecksum, encryptedPackage.sha256Checksum)

        // Decrypt back
        val decryptedJson = cryptoEngine.decrypt(encryptedPackage.base64Payload, passphrase)
        assertEquals(originalJson, decryptedJson)
    }

    @Test
    fun testDecryptionWithWrongPassphrase_fails() {
        val originalJson = """{"test":"confidential"}"""
        val correctPassphrase = "correct_password".toCharArray()
        val wrongPassphrase = "wrong_password".toCharArray()

        val encryptedPackage = cryptoEngine.encrypt(originalJson, correctPassphrase)

        try {
            cryptoEngine.decrypt(encryptedPackage.base64Payload, wrongPassphrase)
            fail("Expected AEADBadTagException or cipher exception on wrong passphrase")
        } catch (e: Exception) {
            // Expected
            assertTrue(e is AEADBadTagException || e.cause is AEADBadTagException || e.message != null)
        }
    }

    @Test
    fun testSha256TamperDetection() {
        val payload = "SGVsbG8gV29ybGQ=" // "Hello World" in Base64
        val originalChecksum = cryptoEngine.calculateSha256(payload)

        val tamperedPayload = "SGVsbG8gV29ybG=="
        val tamperedChecksum = cryptoEngine.calculateSha256(tamperedPayload)

        assertNotEquals(originalChecksum, tamperedChecksum)
    }
}
