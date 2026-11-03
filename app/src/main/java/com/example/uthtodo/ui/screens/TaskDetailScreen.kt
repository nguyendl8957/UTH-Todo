package com.example.uthtodo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uthtodo.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.OutlinedTextField
import java.util.UUID

private val DetailBlue = Color(0xFF2F5BEA)
private val DetailBackground = Color(0xFFF5F5F7)
private val DetailText = Color(0xFF1C1C1E)
private val DetailGray = Color(0xFF6B6B70)
private val DetailRed = Color(0xFFC4321F)
private val DetailDivider = Color(0xFFEDEDF0)

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

    val categoryIsStudy = task.category == HomeTaskCategory.STUDY
    val completedSubtasks = task.subtasks.count { it.done }
    val subtaskProgress = if (task.subtasks.isEmpty()) 0f
    else completedSubtasks.toFloat() / task.subtasks.size

    val priorityLabel = when (task.priority) {
        "low" -> stringResource(R.string.task_priority_low)
        "high" -> stringResource(R.string.task_priority_high)
        else -> stringResource(R.string.task_priority_medium)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.detail_delete_title)) },
            text = { Text(stringResource(R.string.detail_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        stringResource(R.string.detail_delete),
                        color = DetailRed
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.task_editor_cancel))
                }
            }
        )
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
                    tint = DetailBlue,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    stringResource(R.string.tasks_title),
                    color = DetailBlue,
                    fontSize = 16.sp
                )
            }

            TextButton(onClick = onEdit) {
                Text(
                    stringResource(R.string.detail_edit),
                    color = DetailBlue,
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
                                Color(0xFFE9E9EE),
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
                    .background(Color.White)
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

            if (task.note.isNotBlank()) {
                DetailSubtasksSection(
                    task = task,
                    onSubtasksChange = onSubtasksChange
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailSectionTitle(stringResource(R.string.detail_subtasks))
                    Text(
                        "$completedSubtasks/${task.subtasks.size}",
                        color = DetailGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE9E9EE))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(subtaskProgress)
                            .fillMaxHeight()
                            .background(DetailBlue)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                ) {
                    task.subtasks.forEachIndexed { index, subtask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .then(
                                        if (subtask.done) {
                                            Modifier.background(
                                                DetailBlue,
                                                RoundedCornerShape(6.dp)
                                            )
                                        } else {
                                            Modifier.border(
                                                2.dp,
                                                Color(0xFFC7C7CC),
                                                RoundedCornerShape(6.dp)
                                            )
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (subtask.done) {
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
                            Text(
                                subtask.title,
                                color = if (subtask.done) DetailGray
                                else DetailText,
                                fontSize = 15.sp,
                                textDecoration = if (subtask.done) {
                                    TextDecoration.LineThrough
                                } else TextDecoration.None
                            )
                        }

                        if (index != task.subtasks.lastIndex) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(DetailDivider)
                            )
                        }
                    }

                    if (task.subtasks.isEmpty()) {
                        Text(
                            stringResource(R.string.detail_subtasks_empty),
                            modifier = Modifier.padding(16.dp),
                            color = DetailGray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
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
                    containerColor = DetailBlue
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
        color = DetailGray,
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
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
        )
    }
}


@Composable
private fun DetailSubtasksSection(
    task: HomeTask,
    onSubtasksChange: (List<HomeSubtask>) -> Unit
) {
    var editorVisible by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<String?>(null) }
    var inputTitle by remember { mutableStateOf("") }

    var deletingId by remember { mutableStateOf<String?>(null) }

    val completed = task.subtasks.count { it.done }

    // Hộp thoại thêm / sửa việc nhỏ
    if (editorVisible) {
        AlertDialog(
            onDismissRequest = { editorVisible = false },
            title = {
                Text(
                    stringResource(
                        if (editingId == null)
                            R.string.subtask_add_title
                        else
                            R.string.subtask_edit_title
                    )
                )
            },
            text = {
                OutlinedTextField(
                    value = inputTitle,
                    onValueChange = { inputTitle = it },
                    label = {
                        Text(stringResource(R.string.subtask_name))
                    },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    enabled = inputTitle.isNotBlank(),
                    onClick = {
                        val title = inputTitle.trim()
                        val id = editingId

                        val updated = if (id == null) {
                            task.subtasks + HomeSubtask(
                                id = UUID.randomUUID().toString(),
                                title = title,
                                done = false
                            )
                        } else {
                            task.subtasks.map { item ->
                                if (item.id == id) {
                                    item.copy(title = title)
                                } else {
                                    item
                                }
                            }
                        }

                        onSubtasksChange(updated)
                        editorVisible = false
                    }
                ) {
                    Text(stringResource(R.string.detail_saved_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { editorVisible = false }) {
                    Text(stringResource(R.string.task_editor_cancel))
                }
            }
        )
    }

    // Hộp thoại xác nhận xóa việc nhỏ
    if (deletingId != null) {
        AlertDialog(
            onDismissRequest = { deletingId = null },
            title = {
                Text(stringResource(R.string.subtask_delete_title))
            },
            text = {
                Text(stringResource(R.string.subtask_delete_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = deletingId
                        if (id != null) {
                            onSubtasksChange(
                                task.subtasks.filterNot { it.id == id }
                            )
                        }
                        deletingId = null
                    }
                ) {
                    Text(
                        stringResource(R.string.detail_delete),
                        color = Color(0xFFC4321F)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingId = null }) {
                    Text(stringResource(R.string.task_editor_cancel))
                }
            }
        )
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
                color = Color(0xFF6B6B70),
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
                .background(Color(0xFFE9E9EE))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(Color(0xFF2F5BEA))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
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
                                        Color(0xFF2F5BEA)
                                    else
                                        Color.Transparent
                                )
                                .border(
                                    width = if (item.done) 0.dp else 2.dp,
                                    color = if (item.done)
                                        Color.Transparent
                                    else
                                        Color(0xFFC7C7CC),
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

                    // Nhấn tên để sửa
                    Text(
                        text = item.title,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                editingId = item.id
                                inputTitle = item.title
                                editorVisible = true
                            }
                            .padding(vertical = 12.dp),
                        fontSize = 14.sp,
                        color = if (item.done)
                            Color(0xFF6B6B70)
                        else
                            Color(0xFF1C1C1E),
                        textDecoration = if (item.done)
                            TextDecoration.LineThrough
                        else
                            TextDecoration.None
                    )

                    // Xóa việc nhỏ
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_detail_delete
                        ),
                        contentDescription = stringResource(
                            R.string.subtask_delete_title
                        ),
                        tint = Color(0xFF6B6B70),
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
                            .background(Color(0xFFEDEDF0))
                    )
                }
            }

            if (task.subtasks.isEmpty()) {
                Text(
                    stringResource(R.string.detail_subtasks_empty),
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFF6B6B70),
                    fontSize = 14.sp
                )
            }

            TextButton(
                onClick = {
                    editingId = null
                    inputTitle = ""
                    editorVisible = true
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    painter = painterResource(
                        R.drawable.ic_detail_add
                    ),
                    contentDescription = null,
                    tint = Color(0xFF2F5BEA),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    stringResource(R.string.subtask_add),
                    color = Color(0xFF2F5BEA)
                )
            }
        }
    }
}

