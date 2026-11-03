package com.example.uthtodo.notifications

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.example.uthtodo.MainActivity
import com.example.uthtodo.R
import com.example.uthtodo.ui.screens.HomeTask
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

/** Local reminders: no FCM, no exact-alarm privileged permission. Timing is approximate. */
class TodoAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val expectedUid = intent.getStringExtra("uid") ?: return
        if (FirebaseAuth.getInstance().currentUser?.uid != expectedUid) return
        val prefs = context.getSharedPreferences("uth_todo_settings", Context.MODE_PRIVATE)
        val isSummary = intent.getBooleanExtra("summary", false)
        if (!prefs.getBoolean(if (isSummary) "daily_summary" else "reminders", !isSummary)) {
            if (isSummary) TodoNotificationScheduler(context).scheduleDailySummary()
            return
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = if (prefs.getBoolean("sound", true)) "uth_todo_sound" else "uth_todo_silent"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.nav_notifications),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            if (channelId == "uth_todo_silent") channel.setSound(null, null)
            manager.createNotificationChannel(channel)
        }
        val title = if (isSummary) context.getString(R.string.settings_summary)
        else intent.getStringExtra("task_title") ?: return
        val message = if (isSummary) {
            context.getString(R.string.uth_daily_summary_body,
                prefs.getInt("summary_completed", 0), prefs.getInt("summary_pending", 0))
        } else context.getString(R.string.uth_reminder_body)
        val launchIntent = Intent(context, MainActivity::class.java)
        val launchPending = PendingIntent.getActivity(context, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, channelId)
        } else Notification.Builder(context)
        val notification = builder
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(launchPending)
            .setAutoCancel(true)
            .build()
        NotificationHistory.add(context, expectedUid,
            TodoNotificationEntry(title, message, System.currentTimeMillis()))
        try {
            manager.notify(if (isSummary) 97222 else intent.getIntExtra("notification_id", 42), notification)
        } catch (_: SecurityException) {
            // Android 13+: user may decline the notification permission.
        }
        if (isSummary) TodoNotificationScheduler(context).scheduleDailySummary()
    }
}

class TodoNotificationScheduler(private val context: Context) {
    private val prefs = context.getSharedPreferences("uth_todo_settings", Context.MODE_PRIVATE)
    private val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun taskIntent(id: String, title: String): PendingIntent {
        val requestCode = id.hashCode()
        val intent = Intent(context, TodoAlarmReceiver::class.java)
            .putExtra("uid", com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty())
            .putExtra("task_title", title)
            .putExtra("notification_id", requestCode)
        return PendingIntent.getBroadcast(context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun sync(tasks: List<HomeTask>) {
        prefs.edit()
            .putInt("summary_completed", tasks.count { it.done })
            .putInt("summary_pending", tasks.count { !it.done })
            .apply()
        val previous = prefs.getStringSet("scheduled_task_ids", emptySet()).orEmpty().toSet()
        for (id in previous) alarms.cancel(taskIntent(id, ""))
        val next = mutableSetOf<String>()
        if (prefs.getBoolean("reminders", true)) {
            for (task in tasks) {
                if (!task.remind || task.done) continue
                val timestamp = runCatching {
                    val deadline = LocalDateTime.parse("${task.date}T${task.time}")
                    deadline.minusMinutes(15).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }.getOrNull() ?: continue
                if (timestamp <= System.currentTimeMillis()) continue
                alarms.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, timestamp, taskIntent(task.id, task.title)
                )
                next.add(task.id)
            }
        }
        prefs.edit().putStringSet("scheduled_task_ids", next).apply()
        scheduleDailySummary()
    }

    fun scheduleDailySummary() {
        val intent = Intent(context, TodoAlarmReceiver::class.java)
            .putExtra("summary", true)
            .putExtra("uid", com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty())
        val pending = PendingIntent.getBroadcast(context, 97222, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarms.cancel(pending)
        if (!prefs.getBoolean("daily_summary", false)) return
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pending)
    }
}