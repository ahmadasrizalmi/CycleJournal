package com.app.cyclejournal.data.remote

import com.app.cyclejournal.data.remote.model.BackupUploadRequest
import com.app.cyclejournal.data.remote.model.CloudRestoreResult
import com.app.cyclejournal.data.remote.model.RemoteBackupItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * RESTful client for zero-knowledge cloud synchronization with Cloudflare Worker and D1.
 */
class CloudflareBackupClient(private val baseUrl: String) {

    suspend fun uploadMonthlyBackup(request: BackupUploadRequest): Boolean = withContext(Dispatchers.IO) {
        val endpoint = URL("$baseUrl/api/backup")
        val conn = (endpoint.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 15_000
            readTimeout = 15_000
            doOutput = true
        }

        try {
            OutputStreamWriter(conn.outputStream).use { it.write(request.toJsonString()) }
            val code = conn.responseCode
            code in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            conn.disconnect()
        }
    }

    suspend fun fetchBackup(userId: String, month: String? = null): CloudRestoreResult = withContext(Dispatchers.IO) {
        val urlStr = buildString {
            append("$baseUrl/api/backup?user_id=$userId")
            if (!month.isNullOrEmpty()) {
                append("&backup_month=$month")
            }
        }
        val endpoint = URL(urlStr)
        val conn = (endpoint.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            connectTimeout = 15_000
            readTimeout = 15_000
        }

        try {
            val code = conn.responseCode
            if (code == 404) return@withContext CloudRestoreResult.NotFound
            if (code !in 200..299) return@withContext CloudRestoreResult.Error("HTTP error $code")

            val responseText = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)

            if (json.has("data")) {
                val dataObj = json.getJSONObject("data")
                val item = RemoteBackupItem(
                    userId = dataObj.getString("user_id"),
                    backupMonth = dataObj.getString("backup_month"),
                    cipherPayload = dataObj.getString("cipher_payload"),
                    payloadHash = dataObj.getString("payload_hash"),
                    updatedAt = dataObj.getLong("updated_at")
                )
                CloudRestoreResult.Success(item)
            } else if (json.has("available_backups")) {
                val arr = json.getJSONArray("available_backups")
                val months = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    months.add(arr.getJSONObject(i).getString("backup_month"))
                }
                CloudRestoreResult.AvailableMonths(months)
            } else {
                CloudRestoreResult.Error("Malformed server response")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            CloudRestoreResult.Error(e.message ?: "Network error")
        } finally {
            conn.disconnect()
        }
    }

    suspend fun purgeRemoteCloudData(userId: String): Boolean = withContext(Dispatchers.IO) {
        val endpoint = URL("$baseUrl/api/backup?user_id=$userId")
        val conn = (endpoint.openConnection() as HttpURLConnection).apply {
            requestMethod = "DELETE"
            connectTimeout = 15_000
            readTimeout = 15_000
        }
        try {
            val code = conn.responseCode
            code in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            conn.disconnect()
        }
    }
}
