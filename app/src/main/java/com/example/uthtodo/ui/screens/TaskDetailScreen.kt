package com.example.uthtodo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import com.example.uthtodo.ui.components.TodoModalSheet
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uthtodo.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

private val DetailRed = Color(0xFFC4321F)

@Composable
fun TaskDetailScreen(
    task: HomeTask,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
    onSubtasksChange: (List<HomeSubtask>) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    val accent = colorScheme.primary
    val DetailBackground = colorScheme.background
    val DetailText = colorScheme.onBackground
    val DetailGray = colorScheme.onSurfaceVariant
    val DetailDivider = colorScheme.outline.copy(alpha = 0.35f)

    val categoryIsStudy = task.category == HomeTaskCategory.STUDY

    val priorityLabel = when (task.priority) {
        "low" -> stringResource(R.string.task_priority_low)
        "high" -> stringResource(R.string.task_priority_high)
        else -> stringResource(R.string.task_priority_medium)
    }

    if (showDeleteDialog) {
        TodoModalSheet(onDismissRequest = { showDeleteDialog = false }) {
            Text(
                stringResource(R.string.detail_delete_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.detail_delete_message),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    modifier = Modifier.weight(1f).heightIn(min = 50.dp)
                ) { Text(stringResource(R.string.task_editor_cancel)) }
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DetailRed)
                ) { Text(stringResource(R.string.detail_delete)) }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DetailBackground)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_detail_back),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    stringResource(R.string.tasks_title),
                    color = accent,
                    fontSize = 16.sp
                )
            }

            TextButton(onClick = onEdit) {
                Text(
                    stringResource(R.string.detail_edit),
                    color = accent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (categoryIsStudy) Color(0xFFE6EDFF)
                                else Color(0xFFFFEBD9),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            stringResource(
                                if (categoryIsStudy) R.string.home_study
                                else R.string.home_life
                            ),
                            color = if (categoryIsStudy) Color(0xFF1F4FD1)
                            else Color(0xFFA24A08),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            stringResource(
                                R.string.detail_priority_format,
                                priorityLabel
                            ),
                            color = DetailGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = task.title,
                    color = DetailText,
                    fontSize = 26.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (task.done) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_detail_calendar),
                        contentDescription = null,
                        tint = DetailGray,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        stringResource(R.string.detail_due),
                        modifier = Modifier.weight(1f),
                        fontSize = 15.sp,
                        color = DetailGray
                    )
                    Text(
                        formatDetailDate(task.date) + ", " + task.time,
                        fontSize = 15.sp,
                        color = DetailText,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(DetailDivider)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_detail_bell),
                        contentDescription = null,
                        tint = DetailGray,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        stringResource(R.string.detail_reminder),
                        modifier = Modifier.weight(1f),
                        fontSize = 15.sp,
                        color = DetailGray
                    )
                    Text(
                        stringResource(
                            if (task.remind) R.string.task_editor_remind_detail
                            else R.string.detail_reminder_off
                        ),
                        fontSize = 15.sp,
                        color = DetailText
                    )
                }
            }

            DetailSubtasksSection(
                task = task,
                onSubtasksChange = onSubtasksChange
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = 20.dp, end = 20.dp,
                    top = 12.dp, bottom = 12.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = onToggleDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent
                )
            ) {
                Text(
                    stringResource(
                        if (task.done) R.string.detail_mark_pending
                        else R.string.detail_mark_done
                    ),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            TextButton(onClick = { showDeleteDialog = true }) {
                Text(
                    stringResource(R.string.detail_delete),
                    color = DetailRed,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun DetailSectionTitle(text: String) {
    Text(
        text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun formatDetailDate(date: String): String {
    val parsed = runCatching { LocalDate.parse(date) }.getOrNull()
        ?: return date
    val today = LocalDate.now()

    return when (parsed) {
        today -> stringResource(R.string.tasks_today)
        today.plusDays(1) -> stringResource(R.string.tasks_tomorrow)
        else -> parsed.format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy", LocalConfiguration.current.locales[0] ?: Locale.getDefault())
        )
    }
}


@Composable
private fun DetailSubtasksSection(
    task: HomeTask,
    onSubtasksChange: (List<HomeSubtask>) -> Unit
) {
    var editingId by remember { mutableStateOf<String?>(null) }
    var editedTitle by remember { mutableStateOf("") }
    var newTitle by remember { mutableStateOf("") }

    var deletingId by remember { mutableStateOf<String?>(null) }

    val colorScheme = MaterialTheme.colorScheme
    val detailText = colorScheme.onBackground
    val detailGray = colorScheme.onSurfaceVariant
    val detailRed = colorScheme.error
    val detailDivider = colorScheme.outline.copy(alpha = 0.35f)

    val completed = task.subtasks.count { it.done }

    // Hộp thoại xác nhận xóa việc nhỏ
    if (deletingId != null) {
        TodoModalSheet(onDismissRequest = { deletingId = null }) {
            Text(
                stringResource(R.string.subtask_delete_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.subtask_delete_message), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { deletingId = null },
                    modifier = Modifier.weight(1f).heightIn(min = 50.dp)
                ) { Text(stringResource(R.string.task_editor_cancel)) }
                Button(
                    onClick = {
                        deletingId?.let { id -> onSubtasksChange(task.subtasks.filterNot { it.id == id }) }
                        deletingId = null
                    },
                    modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = detailRed)
                ) { Text(stringResource(R.string.detail_delete)) }
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DetailSectionTitle(
                stringResource(R.string.detail_subtasks)
            )

            Text(
                "$completed/${task.subtasks.size}",
                color = detailGray,
                fontSize = 13.sp
            )
        }

        val progress = if (task.subtasks.isEmpty()) {
            0f
        } else {
            completed.toFloat() / task.subtasks.size
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colorScheme.surface)
        ) {
            task.subtasks.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tích hoàn thành
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clickable {
                                onSubtasksChange(
                                    task.subtasks.map { subtask ->
                                        if (subtask.id == item.id) {
                                            subtask.copy(done = !subtask.done)
                                        } else {
                                            subtask
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (item.done)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        Color.Transparent
                                )
                                .border(
                                    width = if (item.done) 0.dp else 2.dp,
                                    color = if (item.done)
                                        Color.Transparent
                                    else
                                        colorScheme.outline,
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.done) {
                                Icon(
                                    painter = painterResource(
                                        R.drawable.ic_detail_check
                                    ),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    if (editingId == item.id) {
                        BasicTextField(
                            value = editedTitle,
                            onValueChange = { editedTitle = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                        TextButton(
                            enabled = editedTitle.isNotBlank(),
                            onClick = {
                                val title = editedTitle.trim()
                                onSubtasksChange(task.subtasks.map { subtask ->
                                    if (subtask.id == item.id) subtask.copy(title = title) else subtask
                                })
                                editingId = null
                            }
                        ) { Text(stringResource(R.string.detail_saved_button)) }
                    } else {
                        Text(
                            text = item.title,
                            modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                            fontSize = 14.sp,
                            color = if (item.done) detailGray else detailText,
                            textDecoration = if (item.done) TextDecoration.LineThrough
                                else TextDecoration.None
                        )
                        Icon(
                            painter = painterResource(R.drawable.ic_detail_edit),
                            contentDescription = stringResource(R.string.subtask_edit_title),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp).clickable {
                                editingId = item.id
                                editedTitle = item.title
                            }
                        )
                    }

                    // Xóa việc nhỏ
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_detail_delete
                        ),
                        contentDescription = stringResource(
                            R.string.subtask_delete_title
                        ),
                        tint = detailRed,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { deletingId = item.id }
                    )
                }

                if (index != task.subtasks.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(detailDivider)
                    )
                }
            }

            if (task.subtasks.isEmpty()) {
                Text(
                    stringResource(R.string.detail_subtasks_empty),
                    modifier = Modifier.padding(16.dp),
                    color = detailGray,
                    fontSize = 14.sp
                )
            }

            if (task.subtasks.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            }
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_detail_add),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                BasicTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (newTitle.isBlank()) {
                                Text(
                                    stringResource(R.string.subtask_add),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    enabled = newTitle.isNotBlank(),
                    onClick = {
                        val title = newTitle.trim()
                        if (title.isNotEmpty()) {
                            onSubtasksChange(
                                task.subtasks + HomeSubtask(UUID.randomUUID().toString(), title)
                            )
                            newTitle = ""
                        }
                    }
                ) {
                    Text(stringResource(R.string.subtask_add_action))
                }
            }
        }
    }
}

