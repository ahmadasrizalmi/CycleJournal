package com.app.cyclejournal.data.backup

import com.app.cyclejournal.data.local.entity.CervicalMucusType
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.data.local.entity.FlowIntensity
import org.json.JSONObject
import java.time.LocalDate

/**
 * Robust JSON parser and serializer for zero-knowledge backup and disaster recovery.
 */
object BackupJsonParser {

    /**
     * Serializes all daily logs and cycle records into standard JSON Version 1 payload.
     */
    fun serializeBackupPayload(
        userId: String,
        logs: List<DailyLogEntity>
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("user_id", userId)
        root.put("exported_at", System.currentTimeMillis())

        val logsArray = org.json.JSONArray()
        for (log in logs) {
            val item = JSONObject()
            item.put("date", log.date.toString())
            item.put("flow", log.flow.name)
            log.basalBodyTempCelsius?.let { item.put("bbt", it) }
            item.put("mucus", log.cervicalMucus.name)
            item.put("pain_vas", log.painVasScore)
            log.painLocation?.let { item.put("pain_loc", it) }
            item.put("analgesic", log.takenAnalgesic)
            log.notes?.let { item.put("notes", it) }
            logsArray.put(item)
        }
        root.put("daily_logs", logsArray)

        return root.toString()
    }

    /**
     * Deserializes decrypted JSON string into a list of [DailyLogEntity] records.
     */
    fun parseDailyLogs(jsonStr: String): List<DailyLogEntity> {
        val root = JSONObject(jsonStr)
        val logsArray = root.optJSONArray("daily_logs") ?: return emptyList()
        val result = mutableListOf<DailyLogEntity>()

        for (i in 0 until logsArray.length()) {
            val item = logsArray.getJSONObject(i)
            val dateStr = item.getString("date")
            val date = LocalDate.parse(dateStr)

            val flow = try {
                FlowIntensity.valueOf(item.optString("flow", FlowIntensity.NONE.name))
            } catch (e: Exception) {
                FlowIntensity.NONE
            }

            val bbt = if (item.has("bbt") && !item.isNull("bbt")) item.getDouble("bbt") else null

            val mucus = try {
                CervicalMucusType.valueOf(item.optString("mucus", CervicalMucusType.NONE.name))
            } catch (e: Exception) {
                CervicalMucusType.NONE
            }

            val painScore = item.optInt("pain_vas", 0)
            val painLoc = if (item.has("pain_loc") && !item.isNull("pain_loc")) item.getString("pain_loc") else null
            val analgesic = item.optBoolean("analgesic", false)
            val notes = if (item.has("notes") && !item.isNull("notes")) item.getString("notes") else null

            result.add(
                DailyLogEntity(
                    date = date,
                    flow = flow,
                    basalBodyTempCelsius = bbt,
                    cervicalMucus = mucus,
                    painVasScore = painScore,
                    painLocation = painLoc,
                    takenAnalgesic = analgesic,
                    notes = notes
                )
            )
        }

        return result
    }
}
