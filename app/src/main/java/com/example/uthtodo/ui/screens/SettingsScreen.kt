package com.example.uthtodo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uthtodo.R
import com.example.uthtodo.ui.components.TodoBottomBar
import com.example.uthtodo.ui.components.TodoTab
import com.example.uthtodo.ui.theme.AppAppearance
import com.example.uthtodo.notifications.TodoNotificationScheduler
import com.example.uthtodo.ui.screens.HomeTask
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.shadow

private val accentColors = listOf(
    Color(0xFF2F5BEA),
    Color(0xFF0F8A5F),
    Color(0xFFC2410C),
    Color(0xFF7C3AED)
)

@Composable
fun SettingsScreen(
    onHomeClick: () -> Unit,
    onTasksClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    appearance: AppAppearance,
    tasksForReminders: List<HomeTask>
) {
    val context = LocalContext.current

    val preferences = remember {
        context.getSharedPreferences(
            "uth_todo_settings",
            android.content.Context.MODE_PRIVATE
        )
    }

    val themeMode = appearance.themeMode
    val accentIndex = appearance.accentIndex
    var languageDialog by remember { mutableStateOf(false) }

    var reminders by remember {
        mutableStateOf(preferences.getBoolean("reminders", true))
    }

    var sound by remember {
        mutableStateOf(preferences.getBoolean("sound", true))
    }

    var dailySummary by remember {
        mutableStateOf(preferences.getBoolean("daily_summary", false))
    }

    if (languageDialog) {
        AlertDialog(
            onDismissRequest = { languageDialog = false },
            title = { Text(stringResource(R.string.settings_language)) },
            text = {
                Column {
                    listOf(
                        "vi" to "Tiếng Việt", "en" to "English",
                        "zh-CN" to "简体中文", "ja" to "日本語", "ko" to "한국어"
                    ).forEach { (tag, label) ->
                        TextButton(onClick = {
                            appearance.setLanguage(tag)
                            languageDialog = false
                        }) { Text(label) }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { languageDialog = false }) {
                    Text(stringResource(R.string.task_editor_cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.settings_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Color(0xFFE6EDFF),
                            CircleShape
                        )
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "UT",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            SettingsSectionLabel(
                stringResource(R.string.settings_appearance)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(18.dp)
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "light" to R.string.settings_light,
                        "dark" to R.string.settings_dark,
                        "system" to R.string.settings_system
                    ).forEach { (mode, labelId) ->
                        val selected = themeMode == mode

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    appearance.setTheme(mode)
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(84.dp)
                                    .background(
                                        when (mode) {
                                            "dark" -> Color.Black
                                            "system" -> Color(0xFF88888C)
                                            else -> MaterialTheme.colorScheme.background
                                        },
                                        RoundedCornerShape(14.dp)
                                    )
                                    .border(
                                        if (selected) 2.dp else 1.dp,
                                        if (selected) accentColors[accentIndex]
                                        else Color(0xFFEDEDF0),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth(0.6f)
                                            .height(8.dp)
                                            .background(
                                                Color(0xFFD1D1D6),
                                                RoundedCornerShape(4.dp)
                                            )
                                    )
                                    repeat(2) {
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .height(18.dp)
                                                .background(
                                                    if (mode == "dark")
                                                        Color(0xFF2C2C2E)
                                                    else Color.White,
                                                    RoundedCornerShape(6.dp)
                                                )
                                        )
                                    }
                                }
                            }

                            Text(
                                stringResource(labelId),
                                fontSize = 13.sp,
                                fontWeight = if (selected)
                                    FontWeight.SemiBold
                                else FontWeight.Normal,
                                color = if (selected)
                                    accentColors[accentIndex]
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFEDEDF0))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.settings_accent),
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row {
                        accentColors.forEachIndexed { index, color ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        appearance.setAccent(index)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .border(
                                            if (accentIndex == index)
                                                2.dp else 0.dp,
                                            color,
                                            CircleShape
                                        )
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        Modifier
                                            .size(22.dp)
                                            .background(color, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            SettingsSectionLabel(
                stringResource(R.string.settings_notifications)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(18.dp)
                    )
            ) {
                SettingsToggleRow(
                    title = stringResource(R.string.settings_reminders),
                    subtitle = stringResource(R.string.settings_reminders_desc),
                    checked = reminders,
                    onCheckedChange = {
                        reminders = it
                        preferences.edit()
                            .putBoolean("reminders", it)
                            .apply()
                        TodoNotificationScheduler(context).sync(tasksForReminders)
                    }
                )

                HorizontalDivider(
                    color = Color(0xFFEDEDF0)
                )

                SettingsToggleRow(
                    title = stringResource(R.string.settings_sound),
                    subtitle = stringResource(R.string.settings_sound_desc),
                    checked = sound,
                    onCheckedChange = {
                        sound = it
                        preferences.edit()
                            .putBoolean("sound", it)
                            .apply()
                    }
                )

                HorizontalDivider(
                    color = Color(0xFFEDEDF0)
                )

                SettingsToggleRow(
                    title = stringResource(R.string.settings_summary),
                    subtitle = stringResource(R.string.settings_summary_desc),
                    checked = dailySummary,
                    onCheckedChange = {
                        dailySummary = it
                        preferences.edit()
                            .putBoolean("daily_summary", it)
                            .apply()
                        TodoNotificationScheduler(context).scheduleDailySummary()
                    }
                )
            }

            SettingsSectionLabel(
                stringResource(R.string.settings_general)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(18.dp)
                    )
            ) {
                SettingsInfoRow(
                    stringResource(R.string.settings_language),
                    when (appearance.languageTag) {
                        "en" -> "English"
                        "zh-CN" -> "简体中文"
                        "ja" -> "日本語"
                        "ko" -> "한국어"
                        else -> "Tiếng Việt"
                    },
                    onClick = { languageDialog = true }
                )

                HorizontalDivider(color = Color(0xFFEDEDF0))

                SettingsInfoRow(
                    stringResource(R.string.settings_about),
                    stringResource(R.string.settings_version)
                )
            }
        }


        TodoBottomBar(
            selectedTab = TodoTab.SETTINGS,
            onHomeClick = onHomeClick,
            onTasksClick = onTasksClick,
            onAddClick = onAddClick,
            onNotificationsClick = onNotificationsClick,
            onSettingsClick = {},
            accent = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Color(0xFF34C759)
            )
        )
    }
}

@Composable
private fun SettingsInfoRow(
    title: String,
    value: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
        Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
