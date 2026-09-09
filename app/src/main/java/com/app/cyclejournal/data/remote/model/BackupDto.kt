package com.app.cyclejournal.data.remote.model

import org.json.JSONObject

data class BackupUploadRequest(
    val userId: String,
    val backupMonth: String,
    val cipherPayload: String,
    val payloadHash: String
) {
    fun toJsonString(): String = JSONObject().apply {
        put("user_id", userId)
        put("backup_month", backupMonth)
        put("cipher_payload", cipherPayload)
        put("payload_hash", payloadHash)
    }.toString()
}

data class RemoteBackupItem(
    val userId: String,
    val backupMonth: String,
    val cipherPayload: String,
    val payloadHash: String,
    val updatedAt: Long
)

sealed class CloudRestoreResult {
    data class Success(val packageItem: RemoteBackupItem) : CloudRestoreResult()
    data class AvailableMonths(val months: List<String>) : CloudRestoreResult()
    object NotFound : CloudRestoreResult()
    data class Error(val message: String) : CloudRestoreResult()
}
