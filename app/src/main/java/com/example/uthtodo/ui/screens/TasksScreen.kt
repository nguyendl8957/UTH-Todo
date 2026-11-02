package com.example.uthtodo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.compose.ui.draw.shadow

private val TasksBlue = Color(0xFF2F5BEA)
private val TasksBackground = Color(0xFFF5F5F7)
private val TasksText = Color(0xFF1C1C1E)
private val TasksSecondary = Color(0xFF6B6B70)

enum class TaskFilter {
    ALL, STUDY, LIFE
}

@Composable
fun TasksScreen(
    tasks: List<HomeTask>,
    initialFilter: TaskFilter = TaskFilter.ALL,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTaskClick: (String) -> Unit,
    onTaskCheckedChange: (String, Boolean) -> Unit
) {
    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    var selectedFilter by remember(initialFilter) {
        mutableStateOf(initialFilter)
    }

    val visibleTasks = tasks.filter { task ->
        val matchesSearch = task.title.contains(
            searchQuery.trim(),
            ignoreCase = true
        )

        val matchesFilter = when (selectedFilter) {
            TaskFilter.ALL -> true
            TaskFilter.STUDY ->
                task.category == HomeTaskCategory.STUDY
            TaskFilter.LIFE ->
                task.category == HomeTaskCategory.LIFE
        }

        matchesSearch && matchesFilter
    }

    val pendingTasks = visibleTasks
        .filterNot { it.done }
        .sortedWith(compareBy({ it.date }, { it.time }))

    val completedTasks = visibleTasks.filter { it.done }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TasksBackground)
            .statusBarsPadding()
    ) {
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.tasks_title),
                        color = TasksText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(
                            R.string.tasks_pending_count,
                            tasks.count { !it.done }
                        ),
                        color = TasksSecondary,
                        fontSize = 14.sp
                    )
                }
            }


            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(
                            Color(0xFFE9E9EE),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_tasks_search),
                        contentDescription = null,
                        tint = TasksSecondary,
                        modifier = Modifier.size(20.dp)
                    )

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = TasksText,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            Box {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = stringResource(
                                            R.string.tasks_search_hint
                                        ),
                                        fontSize = 16.sp,
                                        color = TasksSecondary
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    TaskFilter.entries.forEach { filter ->
                        val title = when (filter) {
                            TaskFilter.ALL ->
                                stringResource(R.string.tasks_all)
                            TaskFilter.STUDY ->
                                stringResource(R.string.home_study)
                            TaskFilter.LIFE ->
                                stringResource(R.string.home_life)
                        }

                        val selected = selectedFilter == filter

                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (selected) TasksText
                                    else Color.White
                                )
                                .then(
                                    if (!selected) Modifier.border(
                                        1.dp,
                                        Color(0xFFD8D8DE),
                                        RoundedCornerShape(18.dp)
                                    ) else Modifier
                                )
                                .clickable {
                                    selectedFilter = filter
                                }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) {
                                    Color.White
                                } else {
                                    TasksText
                                }
                            )
                        }
                    }
                }
            }

            if (visibleTasks.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.tasks_empty),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color.White,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(20.dp),
                        color = TasksSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                if (pendingTasks.isNotEmpty()) {
                    item {
                        TasksSectionTitle(
                            stringResource(R.string.tasks_pending)
                        )
                    }

                    items(
                        pendingTasks,
                        key = { it.id }
                    ) { task ->
                        TasksItem(
                            task = task,
                            onClick = { onTaskClick(task.id) },
                            onToggle = {
                                onTaskCheckedChange(task.id, true)
                            }
                        )
                    }
                }

                if (completedTasks.isNotEmpty()) {
                    item {
                        TasksSectionTitle(
                            stringResource(R.string.tasks_completed)
                        )
                    }

                    items(
                        completedTasks,
                        key = { it.id }
                    ) { task ->
                        TasksItem(
                            task = task,
                            onClick = { onTaskClick(task.id) },
                            onToggle = {
                                onTaskCheckedChange(task.id, false)
                            }
                        )
                    }
                }
            }
        }

        TasksBottomBar(
            onHomeClick = onHomeClick,
            onAddClick = onAddClick,
            onNotificationsClick = onNotificationsClick,
            onSettingsClick = onSettingsClick
        )
    }
}

@Composable
private fun TasksSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = TasksSecondary,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun TasksItem(
    task: HomeTask,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    val isStudy = task.category == HomeTaskCategory.STUDY

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
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
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .then(
                        if (task.done) {
                            Modifier.background(
                                TasksBlue,
                                CircleShape
                            )
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

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick)
                .padding(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = task.title,
                color = if (task.done) {
                    TasksSecondary
                } else {
                    TasksText
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = if (task.done) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!task.done) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(
                            if (isStudy) R.string.home_study
                            else R.string.home_life
                        ),
                        color = if (isStudy) {
                            Color(0xFF1F4FD1)
                        } else {
                            Color(0xFFA24A08)
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .background(
                                if (isStudy) Color(0xFFE6EDFF)
                                else Color(0xFFFFEBD9),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(
                                horizontal = 8.dp,
                                vertical = 2.dp
                            )
                    )

                    Text(
                        text = formatTaskDate(task.date),
                        fontSize = 12.sp,
                        color = TasksSecondary
                    )

                    Text(
                        text = task.time,
                        fontSize = 12.sp,
                        color = TasksSecondary
                    )
                }
            }
        }

        if (!task.done) {
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
private fun formatTaskDate(date: String): String {
    val parsed = runCatching {
        LocalDate.parse(date)
    }.getOrNull() ?: return date

    val today = LocalDate.now()

    return when (parsed) {
        today -> stringResource(R.string.tasks_today)
        today.plusDays(1) ->
            stringResource(R.string.tasks_tomorrow)
        else -> parsed.format(
            DateTimeFormatter.ofPattern(
                "dd/MM",
                Locale.getDefault()
            )
        )
    }
}

@Composable
private fun TasksBottomBar(
    onHomeClick: () -> Unit,
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
        horizontalArrangement =
            Arrangement.SpaceEvenly
    ) {
        TasksNavItem(
            icon = R.drawable.ic_nav_home,
            title = stringResource(R.string.nav_home),
            active = false,
            onClick = onHomeClick,
            modifier = Modifier.weight(1f)
        )

        TasksNavItem(
            icon = R.drawable.ic_nav_tasks,
            title = stringResource(R.string.nav_tasks),
            active = true,
            onClick = {},
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier.weight(1f),
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
                    .background(TasksBlue)
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_nav_add),
                    contentDescription =
                        stringResource(R.string.nav_add),
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        TasksNavItem(
            icon = R.drawable.ic_nav_bell,
            title = stringResource(R.string.nav_notifications),
            active = false,
            onClick = onNotificationsClick,
            modifier = Modifier.weight(1f)
        )

        TasksNavItem(
            icon = R.drawable.ic_nav_settings,
            title = stringResource(R.string.nav_settings),
            active = false,
            onClick = onSettingsClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TasksNavItem(
    icon: Int,
    title: String,
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
            tint = if (active) TasksBlue else TasksSecondary,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (active) {
                FontWeight.SemiBold
            } else {
                FontWeight.Medium
            },
            color = if (active) TasksBlue else TasksSecondary,
            maxLines = 1
        )
    }
}
