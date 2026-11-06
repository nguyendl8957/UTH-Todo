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
import com.example.uthtodo.ui.components.avatarInitials
import com.google.firebase.auth.FirebaseUser
import com.example.uthtodo.ui.components.TodoModalSheet
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build

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
    user: FirebaseUser?,
    tasksForReminders: List<HomeTask>,
    onRequestNotificationPermission: (onResult: (Boolean) -> Unit) -> Unit
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
    val anonymous = user == null || user.isAnonymous
    val accountName = user?.displayName?.trim()?.takeIf { it.isNotBlank() }
        ?: user?.email?.substringBefore('@')?.takeIf { it.isNotBlank() }
    val initials = avatarInitials(accountName, anonymous)
    var languageDialog by remember { mutableStateOf(false) }

    val reminderDefault = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    var reminders by remember {
        mutableStateOf(preferences.getBoolean("reminders", reminderDefault))
    }

    fun updateReminders(enabled: Boolean) {
        reminders = enabled
        preferences.edit().putBoolean("reminders", enabled).apply()
        TodoNotificationScheduler(context).sync(tasksForReminders)
    }

    var sound by remember {
        mutableStateOf(preferences.getBoolean("sound", true))
    }

    var dailySummary by remember {
        mutableStateOf(preferences.getBoolean("daily_summary", false))
    }

    if (languageDialog) {
        TodoModalSheet(onDismissRequest = { languageDialog = false }) {
            Text(
                stringResource(R.string.settings_language),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            listOf(
                "vi" to "Tiếng Việt",
                "en" to "English",
                "zh-CN" to "简体中文",
                "ja" to "日本語",
                "ko" to "한국어"
            ).forEach { (tag, label) ->
                TextButton(
                    onClick = {
                        appearance.setLanguage(tag)
                        languageDialog = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        label,
                        color = if (appearance.languageTag == tag)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { languageDialog = false },
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)
            ) { Text(stringResource(R.string.task_editor_cancel)) }
        }
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
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initials,
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
                            ThemeModePreview(
                                mode = mode,
                                selected = selected,
                                selectionColor = accentColors[accentIndex],
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                stringResource(labelId),
                                fontSize = 15.sp,
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
                    onCheckedChange = { enabled ->
                        if (!enabled) {
                            updateReminders(false)
                        } else if (
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            onRequestNotificationPermission { granted ->
                                if (granted) updateReminders(true)
                            }
                        } else {
                            updateReminders(true)
                        }
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
                    stringResource(
                        when (appearance.languageTag) {
                            "en" -> R.string.settings_language_english
                            "zh-CN" -> R.string.settings_language_chinese
                            "ja" -> R.string.settings_language_japanese
                            "ko" -> R.string.settings_language_korean
                            else -> R.string.settings_language_vietnamese
                        }
                    ),
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
private fun ThemeModePreview(
    mode: String,
    selected: Boolean,
    selectionColor: Color,
    modifier: Modifier = Modifier
) {
    val tileShape = RoundedCornerShape(18.dp)
    Box(modifier.fillMaxWidth().height(108.dp).clip(tileShape)) {
        if (mode == "system") {
            Row(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFF5F5F7))) {
                    ThemeModePreviewPane(
                        isDark = false,
                        modifier = Modifier.fillMaxSize().padding(10.dp)
                    )
                }
                Box(Modifier.weight(1f).fillMaxHeight().background(Color.Black)) {
                    ThemeModePreviewPane(
                        isDark = true,
                        modifier = Modifier.fillMaxSize().padding(10.dp)
                    )
                }
            }
        } else {
            val isDark = mode == "dark"
            Box(
                Modifier.fillMaxSize()
                    .background(if (isDark) Color.Black else Color(0xFFF5F5F7))
            ) {
                ThemeModePreviewPane(
                    isDark = isDark,
                    modifier = Modifier.fillMaxSize().padding(12.dp)
                )
            }
        }
        Box(
            Modifier.fillMaxSize().border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) selectionColor else Color(0xFFE5E5EA),
                shape = tileShape
            )
        )
    }
}

@Composable
private fun ThemeModePreviewPane(isDark: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            Modifier.fillMaxWidth(0.6f).height(9.dp)
                .background(Color(0xFFD1D1D6), RoundedCornerShape(5.dp))
        )
        repeat(2) {
            Box(
                Modifier.fillMaxWidth().weight(1f)
                    .background(
                        if (isDark) Color(0xFF2C2C2E) else Color.White,
                        RoundedCornerShape(7.dp)
                    )
            )
        }
    }
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
