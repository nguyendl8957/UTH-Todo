package com.example.uthtodo.model

enum class TaskCategory(val value: String) {
    STUDY("study"),
    LIFE("life")
}

enum class TaskPriority(val value: String) {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high")
}

data class TaskDraft(
    val title: String = "",
    val note: String = "",
    val category: TaskCategory = TaskCategory.STUDY,
    val date: String = "",
    val time: String = "19:00",
    val remind: Boolean = true,
    val priority: TaskPriority = TaskPriority.MEDIUM
)
