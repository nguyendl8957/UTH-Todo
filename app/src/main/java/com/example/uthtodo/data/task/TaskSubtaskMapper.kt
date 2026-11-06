package com.example.uthtodo.data.task

import com.example.uthtodo.ui.screens.HomeSubtask

/** Maps current and legacy Firestore subtask rows to stable UI identities. */
internal fun mapTaskSubtasks(taskId: String, raw: List<*>?): List<HomeSubtask> =
    raw.orEmpty().mapIndexedNotNull { index, value ->
        val fields = value as? Map<*, *> ?: return@mapIndexedNotNull null
        val title = fields["title"] as? String ?: return@mapIndexedNotNull null
        val storedId = (fields["id"] as? String)?.takeIf(String::isNotBlank)
        HomeSubtask(
            id = storedId ?: "legacy-$taskId-$index",
            title = title,
            done = fields["done"] as? Boolean ?: false
        )
    }
