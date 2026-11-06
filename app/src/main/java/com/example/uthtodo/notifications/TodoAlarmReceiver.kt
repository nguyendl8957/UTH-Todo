package com.example.uthtodo.notifications

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.example.uthtodo.MainActivity
import com.example.uthtodo.R
import com.example.uthtodo.ui.screens.HomeTask
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale

/** Local reminders: no FCM, no exact-alarm privileged permission. Timing is approximate. */
class TodoAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val expectedUid = intent.getStringExtra("uid") ?: return
        if (FirebaseAuth.getInstance().currentUser?.uid != expectedUid) return
        if (intent.getBooleanExtra("prune_notification_history", false)) {
            NotificationHistory.read(context, expectedUid)
            return
        }
        if (intent.getBooleanExtra("cleanup_completed_task", false)) {
            // Legacy 24-hour deletion alarms may still fire after upgrading.
            // Intentionally consume them without touching Firestore.
            return
        }
        val prefs = context.getSharedPreferences("uth_todo_settings", Context.MODE_PRIVATE)
        val locale = Locale.forLanguageTag(prefs.getString("language", "vi") ?: "vi")
        val localizedContext = context.createConfigurationContext(
            Configuration(context.resources.configuration).apply { setLocale(locale) }
        )
        val isSummary = intent.getBooleanExtra("summary", false)
        val isOverdue = intent.getBooleanExtra("overdue", false)
        if (!prefs.getBoolean(if (isSummary) "daily_summary" else "reminders", !isSummary)) {
            if (isSummary) TodoNotificationScheduler(context).scheduleDailySummary()
            return
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = if (prefs.getBoolean("sound", true)) "uth_todo_sound" else "uth_todo_silent"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                localizedContext.getString(R.string.nav_notifications),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            if (channelId == "uth_todo_silent") channel.setSound(null, null)
            manager.createNotificationChannel(channel)
        }
        val title = if (isSummary) localizedContext.getString(R.string.settings_summary)
        else intent.getStringExtra("task_title") ?: return
        val message = if (isSummary) {
            localizedContext.getString(R.string.uth_daily_summary_body,
                prefs.getInt("summary_completed", 0), prefs.getInt("summary_pending", 0))
        } else if (isOverdue) localizedContext.getString(R.string.uth_notification_task_overdue,
            taskTitleFor(intent), intent.getStringExtra("task_time").orEmpty())
        else localizedContext.getString(R.string.uth_reminder_body)
        val notificationId = if (isSummary) 97222 else intent.getIntExtra("notification_id", 42)
        val taskId = intent.getStringExtra("task_id").orEmpty()
        val eventDate = intent.getStringExtra("task_date").orEmpty()
        val eventTime = intent.getStringExtra("task_time").orEmpty()
        val historyId = when {
            isSummary -> "summary-${Calendar.getInstance().get(Calendar.YEAR)}-${Calendar.getInstance().get(Calendar.DAY_OF_YEAR)}"
            isOverdue -> "overdue-$taskId-$eventDate-$eventTime"
            else -> "reminder-$taskId-$eventDate-$eventTime"
        }
        val launchIntent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra("open_notification_id", historyId)
        val launchPending = PendingIntent.getActivity(context, notificationId, launchIntent,
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
        val taskTitle = intent.getStringExtra("task_title").orEmpty()
        val taskTime = intent.getStringExtra("task_time").orEmpty()
        val historyTitle = when {
            isSummary -> title
            isOverdue -> localizedContext.getString(R.string.uth_notification_type_overdue)
            else -> localizedContext.getString(R.string.uth_notification_type_reminder)
        }
        val historyMessage = when {
            isSummary -> message
            isOverdue -> localizedContext.getString(
                R.string.uth_notification_task_overdue, taskTitle, taskTime
            )
            taskTime.isNotBlank() -> localizedContext.getString(
                R.string.uth_notification_task_due, taskTitle, taskTime
            )
            else -> message
        }
        NotificationHistory.add(context, expectedUid,
            TodoNotificationEntry(
                title = historyTitle,
                message = historyMessage,
                timestamp = System.currentTimeMillis(),
                type = when {
                    isSummary -> TodoNotificationType.DAILY_SUMMARY
                    isOverdue -> TodoNotificationType.OVERDUE
                    else -> TodoNotificationType.REMINDER
                },
                id = historyId,
                taskTitle = taskTitle.takeIf { !isSummary && it.isNotBlank() },
                taskTime = taskTime.takeIf { !isSummary && it.isNotBlank() },
                completedCount = if (isSummary) prefs.getInt("summary_completed", 0) else null,
                pendingCount = if (isSummary) prefs.getInt("summary_pending", 0) else null
            ))
        if (isOverdue) {
            val sentKey = "overdue_sent_${expectedUid}"
            val sent = prefs.getStringSet(sentKey, emptySet()).orEmpty().toMutableSet()
            sent += "${intent.getStringExtra("task_id")}-${intent.getStringExtra("task_date")}-${intent.getStringExtra("task_time")}" 
            prefs.edit().putStringSet(sentKey, sent).apply()
        }
        try {
            manager.notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Android 13+: user may decline the notification permission.
        }
        if (isSummary) TodoNotificationScheduler(context).scheduleDailySummary()
    }

    private fun taskTitleFor(intent: Intent): String = intent.getStringExtra("task_title").orEmpty()
}

class TodoNotificationScheduler(private val context: Context) {
    private val prefs = context.getSharedPreferences("uth_todo_settings", Context.MODE_PRIVATE)
    private val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun taskIntent(
        id: String,
        title: String,
        date: String = "",
        time: String = "",
        overdue: Boolean = false
    ): PendingIntent {
        val requestCode = id.hashCode() xor if (overdue) 0x5A17 else 0
        val intent = Intent(context, TodoAlarmReceiver::class.java)
            .putExtra("uid", com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty())
            .putExtra("task_title", title)
            .putExtra("task_date", date)
            .putExtra("task_time", time)
            .putExtra("task_id", id)
            .putExtra("overdue", overdue)
            .putExtra("notification_id", requestCode)
        return PendingIntent.getBroadcast(context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun completedTaskIntent(uid: String, taskId: String): PendingIntent {
        val requestCode = (31 * taskId.hashCode() + uid.hashCode()) xor 0x6D2A
        val intent = Intent(context, TodoAlarmReceiver::class.java)
            .putExtra("uid", uid)
            .putExtra("task_id", taskId)
            .putExtra("cleanup_completed_task", true)
        return PendingIntent.getBroadcast(context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun sync(tasks: List<HomeTask>) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        prefs.edit()
            .putInt("summary_completed", tasks.count { it.done })
            .putInt("summary_pending", tasks.count { !it.done })
            .apply()
        val previous = prefs.getStringSet("scheduled_task_ids", emptySet()).orEmpty().toSet()
        val previousOverdue = prefs.getStringSet("scheduled_overdue_task_ids", emptySet()).orEmpty().toSet()
        val completedKey = "scheduled_completed_task_ids_$uid"
        val previousCompleted = prefs.getStringSet(completedKey, emptySet()).orEmpty().toSet()
        for (id in previous) alarms.cancel(taskIntent(id, ""))
        for (id in previousOverdue) alarms.cancel(taskIntent(id, "", overdue = true))
        for (id in previousCompleted) alarms.cancel(completedTaskIntent(uid, id))
        prefs.edit().remove(completedKey).apply()
        val next = mutableSetOf<String>()
        val nextOverdue = mutableSetOf<String>()
        if (prefs.getBoolean("reminders", true)) {
            for (task in tasks) {
                if (!task.remind || task.done) continue
                val timestamp = runCatching {
                    val deadline = LocalDateTime.parse("${task.date}T${task.time}")
                    deadline.minusMinutes(15).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }.getOrNull() ?: continue
                if (timestamp > System.currentTimeMillis()) {
                    alarms.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timestamp,
                        taskIntent(task.id, task.title, task.date, task.time)
                    )
                    next.add(task.id)
                }
                val overdueSent = prefs.getStringSet("overdue_sent_${com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()}", emptySet()).orEmpty()
                val overdueKey = "${task.id}-${task.date}-${task.time}"
                if (overdueKey !in overdueSent) {
                    val deadline = runCatching {
                        LocalDateTime.parse("${task.date}T${task.time}")
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    }.getOrNull() ?: continue
                    alarms.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        maxOf(deadline, System.currentTimeMillis() + 1000L),
                        taskIntent(task.id, task.title, task.date, task.time, overdue = true)
                    )
                    nextOverdue.add(task.id)
                }
            }
        }
        prefs.edit().putStringSet("scheduled_task_ids", next).apply()
        prefs.edit().putStringSet("scheduled_overdue_task_ids", nextOverdue).apply()
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
