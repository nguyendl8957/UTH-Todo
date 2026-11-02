
package com.example.uthtodo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uthtodo.R
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.ui.draw.shadow

private val Blue = Color(0xFF2F5BEA)
private val Background = Color(0xFFF5F5F7)
private val TextPrimary = Color(0xFF1C1C1E)
private val TextSecondary = Color(0xFF6B6B70)
private val StudyBackground = Color(0xFFE6EDFF)
private val StudyText = Color(0xFF1F4FD1)
private val LifeBackground = Color(0xFFFFEBD9)
private val LifeText = Color(0xFFA24A08)

// Mô hình tạm cho giao diện. Sau này Repository cung cấp dữ liệu.
enum class HomeTaskCategory {
    STUDY, LIFE
}

data class HomeTask(
    val id: String,
    val title: String,
    val category: HomeTaskCategory,
    val date: String, // yyyy-MM-dd
    val time: String, // HH:mm
    val done: Boolean
)

@Composable
fun HomeScreen(
    user: FirebaseUser?,
    tasks: List<HomeTask> = emptyList(),
    hasUnreadNotifications: Boolean = false,
    onAvatarClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onStudyClick: () -> Unit = {},
    onLifeClick: () -> Unit = {},
    onViewAllClick: () -> Unit = {},
    onTaskClick: (String) -> Unit = {},
    onTaskCheckedChange: (String, Boolean) -> Unit = { _, _ -> },
    onAddClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val locale = Locale.getDefault()

    val today = SimpleDateFormat(
        "yyyy-MM-dd", locale
    ).format(Date())

    val displayDate = SimpleDateFormat(
        "EEEE, d MMMM", locale
    ).format(Date()).replaceFirstChar { it.titlecase(locale) }

    val todayTasks = tasks
        .filter { it.date == today }
        .sortedBy { it.time }

    val completedToday = todayTasks.count { it.done }
    val remainingStudy = tasks.count {
        it.category == HomeTaskCategory.STUDY && !it.done
    }
    val remainingLife = tasks.count {
        it.category == HomeTaskCategory.LIFE && !it.done
    }

    val displayName = user?.displayName
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

    val isGuest = user == null || user.isAnonymous

    val initials = if (isGuest) {
        "K"
    } else {
        displayName.orEmpty()
            .split(" ")
            .filter { it.isNotBlank() }
            .takeLast(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifEmpty { "?" }
    }

    val greeting = if (isGuest || displayName == null) {
        stringResource(R.string.home_greeting_guest)
    } else {
        stringResource(R.string.home_greeting_user, displayName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        // Nội dung có thể cuộn, thanh menu nằm cố định ở dưới.
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(StudyBackground)
                            .clickable(onClick = onAvatarClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = StudyText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement =
                            Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = displayDate,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = greeting,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 2
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable(onClick = onNotificationsClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                R.drawable.ic_home_bell
                            ),
                            contentDescription = stringResource(
                                R.string.home_notifications
                            ),
                            modifier = Modifier.size(22.dp),
                            tint = TextPrimary
                        )

                        if (hasUnreadNotifications) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 9.dp, end = 10.dp)
                                    .size(8.dp)
                                    .background(
                                        Color(0xFFE5484D),
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }

            item {
                HomeProgressCard(
                    completed = completedToday,
                    total = todayTasks.size
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    HomeCategoryCard(
                        title = stringResource(R.string.home_study),
                        remaining = remainingStudy,
                        icon = R.drawable.ic_home_study,
                        background = StudyBackground,
                        iconTint = StudyText,
                        onClick = onStudyClick,
                        modifier = Modifier.weight(1f)
                    )

                    HomeCategoryCard(
                        title = stringResource(R.string.home_life),
                        remaining = remainingLife,
                        icon = R.drawable.ic_home_life,
                        background = LifeBackground,
                        iconTint = LifeText,
                        onClick = onLifeClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(
                            R.string.home_today_tasks
                        ),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )

                    Text(
                        text = stringResource(
                            R.string.home_view_all
                        ),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Blue,
                        modifier = Modifier
                            .clickable(onClick = onViewAllClick)
                            .padding(8.dp)
                    )
                }
            }

            if (todayTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color.White,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(
                                R.string.home_empty_tasks
                            ),
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(
                    items = todayTasks,
                    key = { it.id }
                ) { task ->
                    HomeTaskRow(
                        task = task,
                        onClick = { onTaskClick(task.id) },
                        onCheckedChange = {
                            onTaskCheckedChange(task.id, !task.done)
                        }
                    )
                }
            }
        }

        HomeBottomBar(
            onHomeClick = {},
            onTasksClick = onViewAllClick,
            onAddClick = onAddClick,
            onNotificationsClick = onNotificationsClick,
            onSettingsClick = onSettingsClick
        )
    }
}

@Composable
private fun HomeProgressCard(
    completed: Int,
    total: Int
) {
    val progress = if (total == 0) {
        0f
    } else {
        (completed.toFloat() / total).coerceIn(0f, 1f)
    }

    val message = when {
        total == 0 ->
            stringResource(R.string.home_progress_start)
        progress >= 1f ->
            stringResource(R.string.home_progress_complete)
        progress >= 0.5f ->
            stringResource(R.string.home_progress_half)
        else ->
            stringResource(R.string.home_progress_start)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Blue, RoundedCornerShape(22.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.home_today_progress
                ),
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )

            Text(
                text = stringResource(
                    R.string.home_task_progress,
                    completed,
                    total
                ),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }

        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.28f),
                strokeWidth = 6.dp
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun HomeCategoryCard(
    title: String,
    remaining: Int,
    icon: Int,
    background: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    background,
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = iconTint
            )
        }

        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = TextPrimary
        )

        Text(
            text = stringResource(
                R.string.home_remaining_tasks,
                remaining
            ),
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun HomeTaskRow(
    task: HomeTask,
    onClick: () -> Unit,
    onCheckedChange: () -> Unit
) {
    val study = task.category == HomeTaskCategory.STUDY

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(
                start = 8.dp,
                end = 10.dp,
                top = 6.dp,
                bottom = 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onCheckedChange),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .then(
                        if (task.done) {
                            Modifier.background(Blue, CircleShape)
                        } else {
                            Modifier.border(
                                2.dp,
                                Color(0xFFC7C7CC),
                                CircleShape
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (task.done) {
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_home_check
                        ),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (task.done) {
                        TextSecondary
                    } else {
                        TextPrimary
                    },
                    textDecoration = if (task.done) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(
                            if (study) {
                                R.string.home_study
                            } else {
                                R.string.home_life
                            }
                        ),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (study) StudyText else LifeText,
                        modifier = Modifier
                            .background(
                                if (study) {
                                    StudyBackground
                                } else {
                                    LifeBackground
                                },
                                RoundedCornerShape(6.dp)
                            )
                            .padding(
                                horizontal = 8.dp,
                                vertical = 2.dp
                            )
                    )

                    Text(
                        text = task.time,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Icon(
                painter = painterResource(
                    R.drawable.ic_home_chevron
                ),
                contentDescription = null,
                tint = Color(0xFFAEAEB2),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun HomeBottomBar(
    onHomeClick: () -> Unit,
    onTasksClick: () -> Unit,
    onAddClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .height(70.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        HomeNavItem(
            icon = R.drawable.ic_nav_home,
            label = stringResource(R.string.nav_home),
            active = true,
            onClick = onHomeClick,
            modifier = Modifier.weight(1f)
        )

        HomeNavItem(
            icon = R.drawable.ic_nav_tasks,
            label = stringResource(R.string.nav_tasks),
            active = false,
            onClick = onTasksClick,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .offset(y = (-16).dp)
                    .size(56.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        clip = false
                    )
                    .clip(CircleShape)
                    .background(Blue)
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        R.drawable.ic_nav_add
                    ),
                    contentDescription = stringResource(
                        R.string.nav_add
                    ),
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        HomeNavItem(
            icon = R.drawable.ic_nav_bell,
            label = stringResource(R.string.nav_notifications),
            active = false,
            onClick = onNotificationsClick,
            modifier = Modifier.weight(1f)
        )

        HomeNavItem(
            icon = R.drawable.ic_nav_settings,
            label = stringResource(R.string.nav_settings),
            active = false,
            onClick = onSettingsClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HomeNavItem(
    icon: Int,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (active) Blue else TextSecondary,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = label,
            color = if (active) Blue else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (active) {
                FontWeight.SemiBold
            } else {
                FontWeight.Medium
            },
            maxLines = 1
        )
    }
}
