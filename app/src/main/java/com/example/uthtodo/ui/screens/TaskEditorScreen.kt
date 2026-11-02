package com.example.uthtodo.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import com.example.uthtodo.R
import com.example.uthtodo.model.TaskCategory
import com.example.uthtodo.model.TaskDraft
import com.example.uthtodo.model.TaskPriority
import java.time.DayOfWeek
import java.time.LocalDate

private val EditorBlue = Color(0xFF2F5BEA)
private val EditorBlueText = Color(0xFF1F4FD1)
private val EditorBlueLight = Color(0xFFE6EDFF)
private val EditorBackground = Color(0xFFF5F5F7)
private val EditorWhite = Color.White
private val EditorText = Color(0xFF1C1C1E)
private val EditorSecondary = Color(0xFF6B6B70)
private val EditorBorder = Color(0xFFD8D8DE)
private val EditorDivider = Color(0xFFEDEDF0)
private val EditorControl = Color(0xFFE9E9EE)
private val EditorGreen = Color(0xFF34C759)

@Composable
fun TaskEditorScreen(
    initialDraft: TaskDraft? = null,
    onCancel: () -> Unit,
    onSave: (TaskDraft) -> Unit
) {
    val context = LocalContext.current
    val editing = initialDraft != null

    var title by remember(initialDraft) {
        mutableStateOf(initialDraft?.title.orEmpty())
    }

    var note by remember(initialDraft) {
        mutableStateOf(initialDraft?.note.orEmpty())
    }

    var category by remember(initialDraft) {
        mutableStateOf(
            initialDraft?.category ?: TaskCategory.STUDY
        )
    }

    var date by remember(initialDraft) {
        mutableStateOf(
            initialDraft?.date ?: LocalDate.now().toString()
        )
    }

    var time by remember(initialDraft) {
        mutableStateOf(initialDraft?.time ?: "19:00")
    }

    var remind by remember(initialDraft) {
        mutableStateOf(initialDraft?.remind ?: true)
    }

    var priority by remember(initialDraft) {
        mutableStateOf(
            initialDraft?.priority ?: TaskPriority.MEDIUM
        )
    }

    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
    val sunday = today.plusDays(
        (DayOfWeek.SUNDAY.value - today.dayOfWeek.value).toLong()
    )

    val canSave = title.isNotBlank()

    val saveTask = {
        if (canSave) {
            onSave(
                TaskDraft(
                    title = title.trim(),
                    note = note.trim(),
                    category = category,
                    date = date,
                    time = time,
                    remind = remind,
                    priority = priority
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EditorBackground)
            .statusBarsPadding()
            .imePadding()
    ) {

        // HEADER: HỦY - VIỆC MỚI - LƯU
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                TextButton(onClick = onCancel) {
                    Text(
                        text = stringResource(
                            R.string.task_editor_cancel
                        ),
                        color = EditorBlue,
                        fontSize = 16.sp
                    )
                }
            }

            Text(
                text = stringResource(
                    if (editing) R.string.task_editor_edit
                    else R.string.task_editor_new
                ),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = EditorText
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(
                    onClick = saveTask,
                    enabled = canSave
                ) {
                    Text(
                        text = stringResource(
                            R.string.task_editor_save
                        ),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (canSave) {
                            EditorBlue
                        } else {
                            Color(0xFF8A8A8E)
                        }
                    )
                }
            }
        }

        // NỘI DUNG CUỘN
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 20.dp,
                    vertical = 8.dp
                ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // TÊN CÔNG VIỆC + GHI CHÚ CHUNG MỘT THẺ
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(EditorWhite)
            ) {
                EditorInput(
                    label = stringResource(R.string.task_editor_title),
                    placeholder = stringResource(
                        R.string.task_editor_title_hint
                    ),
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(EditorDivider)
                )

                EditorInput(
                    label = stringResource(R.string.task_editor_note),
                    placeholder = stringResource(
                        R.string.task_editor_note_hint
                    ),
                    value = note,
                    onValueChange = { note = it },
                    singleLine = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        )
                )
            }

            // NHÓM
            EditorSection(
                title = stringResource(R.string.task_editor_category)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EditorChoice(
                        text = stringResource(R.string.home_study),
                        selected = category == TaskCategory.STUDY,
                        onClick = { category = TaskCategory.STUDY },
                        modifier = Modifier.weight(1f),
                        height = 48.dp
                    )

                    EditorChoice(
                        text = stringResource(R.string.home_life),
                        selected = category == TaskCategory.LIFE,
                        onClick = { category = TaskCategory.LIFE },
                        modifier = Modifier.weight(1f),
                        height = 48.dp
                    )
                }
            }

            // NGÀY
            EditorSection(
                title = stringResource(R.string.task_editor_date)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        today to R.string.tasks_today,
                        tomorrow to R.string.tasks_tomorrow,
                        sunday to R.string.tasks_this_week
                    ).forEach { (day, label) ->
                        EditorChoice(
                            text = stringResource(label),
                            selected = date == day.toString(),
                            onClick = { date = day.toString() },
                            modifier = Modifier.weight(1f),
                            height = 44.dp
                        )
                    }
                }
            }

            // GIỜ VÀ NHẮC NHỞ CHUNG MỘT THẺ
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(EditorWhite)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_editor_clock
                        ),
                        contentDescription = null,
                        tint = EditorSecondary,
                        modifier = Modifier.size(22.dp)
                    )

                    Text(
                        text = stringResource(R.string.task_editor_time),
                        modifier = Modifier.weight(1f),
                        fontSize = 15.sp,
                        color = EditorText
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EditorControl)
                            .clickable {
                                val parts = time.split(":")
                                val hour = parts.getOrNull(0)
                                    ?.toIntOrNull() ?: 19
                                val minute = parts.getOrNull(1)
                                    ?.toIntOrNull() ?: 0

                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        time = String.format(
                                            java.util.Locale.US,
                                            "%02d:%02d",
                                            h,
                                            m
                                        )
                                    },
                                    hour,
                                    minute,
                                    true
                                ).show()
                            }
                            .padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = time,
                            fontSize = 15.sp,
                            color = EditorText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(EditorDivider)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_editor_bell
                        ),
                        contentDescription = null,
                        tint = EditorSecondary,
                        modifier = Modifier.size(22.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(
                                R.string.task_editor_remind
                            ),
                            fontSize = 15.sp,
                            color = EditorText
                        )

                        Text(
                            text = stringResource(
                                R.string.task_editor_remind_detail
                            ),
                            fontSize = 12.sp,
                            color = EditorSecondary
                        )
                    }

                    // SWITCH 51x31, XANH LÁ ĐÚNG MẪU
                    Box(
                        modifier = Modifier
                            .size(width = 51.dp, height = 31.dp)
                            .clip(CircleShape)
                            .background(
                                if (remind) EditorGreen
                                else Color(0xFFD1D1D6)
                            )
                            .clickable(
                                role = Role.Switch,
                                onClick = { remind = !remind }
                            )
                            .padding(2.dp),
                        contentAlignment = if (remind) {
                            Alignment.CenterEnd
                        } else {
                            Alignment.CenterStart
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(27.dp)
                                .shadow(2.dp, CircleShape)
                                .background(EditorWhite, CircleShape)
                        )
                    }
                }
            }

            // MỨC ƯU TIÊN DẠNG THANH LIỀN
            EditorSection(
                title = stringResource(R.string.task_editor_priority)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EditorControl)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TaskPriority.entries.forEach { item ->
                        val selected = priority == item

                        val text = when (item) {
                            TaskPriority.LOW ->
                                stringResource(R.string.task_priority_low)
                            TaskPriority.MEDIUM ->
                                stringResource(R.string.task_priority_medium)
                            TaskPriority.HIGH ->
                                stringResource(R.string.task_priority_high)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .then(
                                    if (selected) {
                                        Modifier.shadow(
                                            elevation = 2.dp,
                                            shape = RoundedCornerShape(9.dp)
                                        )
                                    } else Modifier
                                )
                                .clip(RoundedCornerShape(9.dp))
                                .background(
                                    if (selected) EditorWhite
                                    else Color.Transparent
                                )
                                .clickable {
                                    priority = item
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = text,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) {
                                    EditorText
                                } else {
                                    EditorSecondary
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // NÚT LƯU CỐ ĐỊNH DƯỚI MÀN
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(EditorBackground)
                .navigationBarsPadding()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 12.dp,
                    bottom = 12.dp
                )
        ) {
            Button(
                onClick = saveTask,
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EditorBlue,
                    disabledContainerColor = EditorControl,
                    disabledContentColor = Color(0xFF8A8A8E)
                )
            ) {
                Text(
                    text = stringResource(
                        R.string.task_editor_save_task
                    ),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun EditorInput(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = EditorSecondary
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            textStyle = TextStyle(
                fontSize = 15.sp,
                color = EditorText
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 15.sp,
                            color = Color(0xFF8A8A8E)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun EditorSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = EditorSecondary
        )
        content()
    }
}

@Composable
private fun EditorChoice(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp
) {
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(
                if (selected) EditorBlueLight else EditorWhite
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) {
                    EditorBlue
                } else {
                    EditorBorder
                },
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) {
                EditorBlueText
            } else {
                EditorText
            }
        )
    }
}
