package com.example.uthtodo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.luminance
import com.example.uthtodo.R
import com.example.uthtodo.notifications.TodoNotificationEntry
import com.example.uthtodo.notifications.TodoNotificationType
import com.example.uthtodo.ui.components.TodoBottomBar
import com.example.uthtodo.ui.components.TodoTab
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    entries: List<TodoNotificationEntry>,
    onReadNotification: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onHomeClick: () -> Unit,
    onTasksClick: () -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    val unreadExists = entries.any { !it.isRead }

    Column(Modifier.fillMaxSize().background(colors.background).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                stringResource(R.string.nav_notifications), fontSize = 28.sp,
                fontWeight = FontWeight.Bold, color = colors.onBackground,
                modifier = Modifier.weight(1f)
            )
            if (unreadExists) {
                TextButton(onClick = onMarkAllRead) {
                    Text(
                        stringResource(R.string.uth_notifications_mark_all_read),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (entries.isEmpty()) {
            Box(
                Modifier.weight(1f).fillMaxWidth().padding(20.dp)
                    .background(colors.surface, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.uth_notifications_empty),
                    modifier = Modifier.padding(20.dp), fontSize = 14.sp,
                    color = colors.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                item(key = "notification-history") {
                    Column(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.surface)
                    ) {
                        entries.forEachIndexed { index, entry ->
                            NotificationRow(
                                entry = entry,
                                locale = locale,
                                onClick = { onReadNotification(entry.id) }
                            )
                            if (index < entries.lastIndex) {
                                HorizontalDivider(color = colors.outline.copy(alpha = 0.45f))
                            }
                        }
                    }
                }
            }
        }

        TodoBottomBar(
            selectedTab = TodoTab.NOTIFICATIONS,
            onHomeClick = onHomeClick,
            onTasksClick = onTasksClick,
            onAddClick = onAddClick,
            onNotificationsClick = {},
            onSettingsClick = onSettingsClick
        )
    }
}

@Composable
private fun NotificationRow(
    entry: TodoNotificationEntry,
    locale: Locale,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typeLabel = when (entry.type) {
        TodoNotificationType.REMINDER -> stringResource(R.string.uth_notification_type_reminder)
        TodoNotificationType.DAILY_SUMMARY -> stringResource(R.string.uth_notification_type_summary)
        TodoNotificationType.COMPLETION -> stringResource(R.string.uth_notification_type_completion)
        TodoNotificationType.OVERDUE -> stringResource(R.string.uth_notification_type_overdue)
        TodoNotificationType.OTHER -> stringResource(R.string.uth_notification_type_other)
    }
    val icon = when (entry.type) {
        TodoNotificationType.REMINDER -> R.drawable.ic_notification_reminder
        TodoNotificationType.DAILY_SUMMARY -> R.drawable.ic_home_bell
        TodoNotificationType.COMPLETION -> R.drawable.ic_detail_check
        TodoNotificationType.OVERDUE -> R.drawable.ic_notification_overdue
        TodoNotificationType.OTHER -> R.drawable.ic_home_bell
    }
    val timeLabel = notificationTimeLabel(
        timestamp = entry.timestamp,
        yesterdayLabel = stringResource(R.string.uth_notification_yesterday),
        locale = locale
    )
    val dark = colors.background.luminance() < 0.5f
    val completion = entry.type == TodoNotificationType.COMPLETION
    val overdue = entry.type == TodoNotificationType.OVERDUE
    val legacyCompletionTitle = listOf(
        "You completed: ", "Bạn đã hoàn thành công việc: ", "你已完成：",
        "完了しました：", "완료한 작업: "
    ).firstNotNullOfOrNull { prefix ->
        entry.message.takeIf { completion && it.startsWith(prefix) }?.removePrefix(prefix)
    }
    val reminderSeparator = listOf(" at ", " lúc ", "，时间 ", "（", " · ")
        .firstOrNull { entry.message.contains(it) }
    val oldReminderTitle = if (entry.type == TodoNotificationType.REMINDER && entry.taskTitle == null && reminderSeparator != null) {
        entry.message.substringBeforeLast(reminderSeparator).trim().takeIf { it.isNotBlank() }
    } else null
    val oldReminderTime = if (oldReminderTitle != null && reminderSeparator != null) {
        entry.message.substringAfterLast(reminderSeparator).trim().trimEnd('.', '。', ')', '）')
    } else null
    val taskTitle = entry.taskTitle ?: legacyCompletionTitle ?: oldReminderTitle
    val shownTitle = when {
        completion && taskTitle != null -> stringResource(R.string.uth_notification_type_completion)
        overdue && taskTitle != null -> stringResource(R.string.uth_notification_type_overdue)
        entry.type == TodoNotificationType.REMINDER && taskTitle != null -> stringResource(R.string.uth_notification_type_reminder)
        entry.type != TodoNotificationType.OTHER -> typeLabel
        else -> entry.title
    }
    val shownMessage = when {
        completion && taskTitle != null -> stringResource(R.string.uth_notification_task_completed, taskTitle)
        overdue && taskTitle != null -> stringResource(R.string.uth_notification_task_overdue, taskTitle, entry.taskTime.orEmpty())
        entry.type == TodoNotificationType.REMINDER && taskTitle != null && (entry.taskTime ?: oldReminderTime) != null ->
            stringResource(R.string.uth_notification_task_due, taskTitle, entry.taskTime ?: oldReminderTime!!)
        entry.type == TodoNotificationType.DAILY_SUMMARY && entry.completedCount != null && entry.pendingCount != null ->
            stringResource(R.string.uth_daily_summary_body, entry.completedCount, entry.pendingCount)
        entry.type == TodoNotificationType.DAILY_SUMMARY -> stringResource(R.string.uth_notification_summary_generic)
        else -> entry.message
    }
    val iconTint = when {
        overdue -> colors.error
        completion && !dark -> androidx.compose.ui.graphics.Color(0xFF0F7A4F)
        else -> colors.primary
    }
    val iconBackground = when {
        completion && !dark -> androidx.compose.ui.graphics.Color(0xFFDDF5EA)
        overdue && !dark -> androidx.compose.ui.graphics.Color(0xFFFBE8E7)
        overdue -> colors.error.copy(alpha = 0.15f)
        completion -> colors.surfaceVariant
        else -> colors.primary.copy(alpha = 0.12f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (entry.isRead) colors.surface else colors.primary.copy(alpha = 0.08f)
            )
            .clickable(
                enabled = !entry.isRead,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier.size(40.dp).background(iconBackground, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = typeLabel,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    shownTitle,
                    modifier = Modifier.weight(1f),
                    color = colors.onSurface,
                    fontSize = 16.sp,
                    fontWeight = if (entry.isRead) FontWeight.Medium else FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(timeLabel, color = colors.onSurfaceVariant, fontSize = 12.sp)
            }
            Text(
                shownMessage,
                color = colors.onSurfaceVariant,
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (!entry.isRead) {
            Box(Modifier.size(8.dp).background(colors.primary, CircleShape))
        }
    }
}

private fun notificationTimeLabel(timestamp: Long, yesterdayLabel: String, locale: Locale): String {
    val eventDate = Date(timestamp)
    val today = SimpleDateFormat("yyyyMMdd", locale).format(Date())
    val eventDay = SimpleDateFormat("yyyyMMdd", locale).format(eventDate)
    if (eventDay == today) return SimpleDateFormat("HH:mm", locale).format(eventDate)

    val calendar = java.util.Calendar.getInstance().apply {
        time = Date()
        add(java.util.Calendar.DAY_OF_YEAR, -1)
    }
    if (SimpleDateFormat("yyyyMMdd", locale).format(calendar.time) == eventDay) {
        return yesterdayLabel
    }
    return DateFormat.getDateInstance(DateFormat.SHORT, locale).format(eventDate)
}
