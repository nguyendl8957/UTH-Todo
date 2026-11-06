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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uthtodo.R
import com.example.uthtodo.ui.components.TodoBottomBar
import com.example.uthtodo.ui.components.TodoTab
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.compose.ui.draw.shadow

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
            .background(MaterialTheme.colorScheme.background)
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
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(
                            R.string.tasks_pending_count,
                            tasks.count { !it.done }
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    if (selected) MaterialTheme.colorScheme.onBackground
                                    else MaterialTheme.colorScheme.surface
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
                                    MaterialTheme.colorScheme.background
                                } else {
                                    MaterialTheme.colorScheme.onBackground
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
                            .background(MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(20.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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

        TodoBottomBar(
            selectedTab = TodoTab.TASKS,
            onTasksClick = {},
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
        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            .background(MaterialTheme.colorScheme.surface)
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
                                MaterialTheme.colorScheme.primary,
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
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
                fontSize = 18.sp,
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
                        fontSize = 14.sp,
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
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = task.time,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                LocalConfiguration.current.locales[0] ?: Locale.getDefault()
            )
        )
    }
}
