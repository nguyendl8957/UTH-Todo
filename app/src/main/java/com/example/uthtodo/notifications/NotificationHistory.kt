package com.example.uthtodo.notifications

import android.content.Context
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class TodoNotificationType(val value: String) {
    REMINDER("reminder"),
    DAILY_SUMMARY("daily_summary"),
    COMPLETION("completion"),
    OVERDUE("overdue"),
    OTHER("other");

    companion object {
        fun from(value: String?): TodoNotificationType =
            entries.firstOrNull { it.value == value } ?: OTHER
    }
}

data class TodoNotificationEntry(
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: TodoNotificationType = TodoNotificationType.OTHER,
    val id: String = UUID.randomUUID().toString(),
    val readAt: Long? = null,
    val taskTitle: String? = null,
    val taskTime: String? = null,
    val completedCount: Int? = null,
    val pendingCount: Int? = null
) {
    val isRead: Boolean get() = readAt != null
}

/** Local-only history, partitioned by Firebase UID. */
object NotificationHistory {
    private const val RETENTION_MS = 24L * 60L * 60L * 1000L
    private fun key(uid: String) = "notifications_$uid"

    @Synchronized
    fun read(context: Context, uid: String): List<TodoNotificationEntry> {
        val prefs = context.getSharedPreferences("uth_notification_history", Context.MODE_PRIVATE)
        val raw = prefs.getString(key(uid), "[]") ?: "[]"
        val parsed = runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val item = array.getJSONObject(i)
                val timestamp = item.optLong("timestamp")
                val readAt = if (item.has("readAt") && !item.isNull("readAt")) {
                    item.optLong("readAt")
                } else null
                TodoNotificationEntry(
                    title = item.optString("title"),
                    message = item.optString("message"),
                    timestamp = timestamp,
                    type = TodoNotificationType.from(item.optString("type")),
                    // Legacy rows had no ID or read state; keep them visible as unread.
                    id = item.optString("id").takeIf(String::isNotBlank)
                        ?: "legacy-$timestamp-$i",
                    readAt = readAt,
                    taskTitle = if (item.isNull("taskTitle")) null
                        else item.optString("taskTitle").takeIf(String::isNotBlank),
                    taskTime = if (item.isNull("taskTime")) null
                        else item.optString("taskTime").takeIf(String::isNotBlank),
                    completedCount = if (item.has("completedCount") && !item.isNull("completedCount")) item.optInt("completedCount") else null,
                    pendingCount = if (item.has("pendingCount") && !item.isNull("pendingCount")) item.optInt("pendingCount") else null
                )
            }
        }.getOrDefault(emptyList())
        val cutoff = System.currentTimeMillis() - RETENTION_MS
        val retained = parsed.filter { it.timestamp >= cutoff }
        if (retained.size != parsed.size) write(context, uid, retained)
        return retained
    }

    @Synchronized
    fun add(context: Context, uid: String, entry: TodoNotificationEntry) {
        val existing = read(context, uid)
        val duplicateEvent = existing.any { previous ->
            previous.id == entry.id || (
                previous.type == entry.type &&
                    previous.title == entry.title &&
                    previous.message == entry.message &&
                    previous.taskTitle == entry.taskTitle &&
                    previous.taskTime == entry.taskTime &&
                    kotlin.math.abs(previous.timestamp - entry.timestamp) <= 5_000L
                )
        }
        if (duplicateEvent) return
        val all = (listOf(entry) + existing).take(50)
        write(context, uid, all)
        scheduleExpiry(context, uid, entry)
    }

    fun rescheduleExpirations(context: Context, uid: String) {
        read(context, uid).forEach { scheduleExpiry(context, uid, it) }
    }

    private fun scheduleExpiry(context: Context, uid: String, entry: TodoNotificationEntry) {
        val requestCode = entry.id.hashCode() xor 0x3F101
        val expiryIntent = Intent(context, TodoAlarmReceiver::class.java)
            .putExtra("uid", uid)
            .putExtra("prune_notification_history", true)
        val expiryPendingIntent = PendingIntent.getBroadcast(
            context, requestCode, expiryIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            maxOf(entry.timestamp + RETENTION_MS, System.currentTimeMillis() + 1_000L),
            expiryPendingIntent
        )
    }

    @Synchronized
    fun markRead(context: Context, uid: String, id: String): List<TodoNotificationEntry> {
        val now = System.currentTimeMillis()
        val all = read(context, uid).map { entry ->
            if (entry.id == id && !entry.isRead) entry.copy(readAt = now) else entry
        }
        write(context, uid, all)
        return all
    }

    @Synchronized
    fun markAllRead(context: Context, uid: String): List<TodoNotificationEntry> {
        val now = System.currentTimeMillis()
        val all = read(context, uid).map { entry ->
            if (entry.isRead) entry else entry.copy(readAt = now)
        }
        write(context, uid, all)
        return all
    }

    private fun write(context: Context, uid: String, entries: List<TodoNotificationEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(JSONObject()
                .put("id", entry.id)
                .put("type", entry.type.value)
                .put("title", entry.title)
                .put("message", entry.message)
                .put("timestamp", entry.timestamp)
                .put("readAt", entry.readAt ?: JSONObject.NULL)
                .put("taskTitle", entry.taskTitle ?: JSONObject.NULL)
                .put("taskTime", entry.taskTime ?: JSONObject.NULL)
                .put("completedCount", entry.completedCount ?: JSONObject.NULL)
                .put("pendingCount", entry.pendingCount ?: JSONObject.NULL))
        }
        context.getSharedPreferences("uth_notification_history", Context.MODE_PRIVATE)
            .edit().putString(key(uid), array.toString()).apply()
    }
}
