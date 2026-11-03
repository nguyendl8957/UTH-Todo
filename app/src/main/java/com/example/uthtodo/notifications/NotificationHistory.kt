package com.example.uthtodo.notifications

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class TodoNotificationEntry(
    val title: String,
    val message: String,
    val timestamp: Long
)

/** Local-only history, partitioned by Firebase UID. */
object NotificationHistory {
    private fun key(uid: String) = "notifications_$uid"

    fun read(context: Context, uid: String): List<TodoNotificationEntry> {
        val prefs = context.getSharedPreferences("uth_notification_history", Context.MODE_PRIVATE)
        val raw = prefs.getString(key(uid), "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val item = array.getJSONObject(i)
                TodoNotificationEntry(
                    item.optString("title"), item.optString("message"),
                    item.optLong("timestamp")
                )
            }
        }.getOrDefault(emptyList())
    }

    fun add(context: Context, uid: String, entry: TodoNotificationEntry) {
        val all = (listOf(entry) + read(context, uid)).take(50)
        val array = JSONArray()
        all.forEach {
            array.put(JSONObject().put("title", it.title)
                .put("message", it.message).put("timestamp", it.timestamp))
        }
        context.getSharedPreferences("uth_notification_history", Context.MODE_PRIVATE)
            .edit().putString(key(uid), array.toString()).apply()
    }
}