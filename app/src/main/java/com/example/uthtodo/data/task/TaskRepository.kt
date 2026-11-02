package com.example.uthtodo.data.task

import com.example.uthtodo.model.TaskDraft
import com.example.uthtodo.ui.screens.HomeTask
import com.example.uthtodo.ui.screens.HomeTaskCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

import com.example.uthtodo.ui.screens.HomeSubtask

class TaskRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun tasksCollection() =
        auth.currentUser?.uid?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("tasks")
        }

    // CREATE: Thêm công việc thật vào Firestore
    fun addTask(
        draft: TaskDraft,
        onResult: (Result<String>) -> Unit
    ) {
        val collection = tasksCollection()

        if (collection == null) {
            onResult(
                Result.failure(
                    IllegalStateException("Chưa đăng nhập Firebase")
                )
            )
            return
        }

        val document = collection.document()

        val taskData = hashMapOf(
            "title" to draft.title,
            "note" to draft.note,
            "category" to draft.category.value,
            "date" to draft.date,
            "time" to draft.time,
            "remind" to draft.remind,
            "priority" to draft.priority.value,
            "done" to false,
            "subtasks" to emptyList<Map<String, Any>>(),
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        document.set(taskData)
            .addOnSuccessListener {
                onResult(Result.success(document.id))
            }
            .addOnFailureListener { error ->
                onResult(Result.failure(error))
            }
    }

    // READ: Theo dõi công việc theo thời gian thực
    fun observeTasks(
        onResult: (Result<List<HomeTask>>) -> Unit
    ): ListenerRegistration? {
        val collection = tasksCollection()

        if (collection == null) {
            onResult(
                Result.failure(
                    IllegalStateException("Chưa đăng nhập Firebase")
                )
            )
            return null
        }

        return collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onResult(Result.failure(error))
                return@addSnapshotListener
            }

            if (snapshot == null) {
                onResult(
                    Result.failure(
                        IllegalStateException("Không nhận được dữ liệu")
                    )
                )
                return@addSnapshotListener
            }

            val tasks = snapshot.documents.mapNotNull { document ->
                val title = document.getString("title")
                    ?: return@mapNotNull null

                val category = when (
                    document.getString("category")
                ) {
                    "study" -> HomeTaskCategory.STUDY
                    "life" -> HomeTaskCategory.LIFE
                    else -> return@mapNotNull null
                }


                val subtasks = (document.get("subtasks") as? List<*>)
                    .orEmpty()
                    .mapNotNull { raw ->
                        val item = raw as? Map<*, *>
                            ?: return@mapNotNull null

                        val subtaskId = item["id"] as? String
                            ?: return@mapNotNull null

                        val subtaskTitle = item["title"] as? String
                            ?: return@mapNotNull null

                        HomeSubtask(
                            id = subtaskId,
                            title = subtaskTitle,
                            done = item["done"] as? Boolean ?: false
                        )
                    }

                HomeTask(
                    id = document.id,
                    title = title,
                    category = category,
                    date = document.getString("date").orEmpty(),
                    time = document.getString("time").orEmpty(),
                    done = document.getBoolean("done") ?: false,
                    note = document.getString("note").orEmpty(),
                    priority = document.getString("priority") ?: "medium",
                    remind = document.getBoolean("remind") ?: true,
                    subtasks = subtasks
                )
            }

            onResult(Result.success(tasks))
        }
    }

    // UPDATE: Đánh dấu hoàn thành / chưa hoàn thành
    fun updateTaskDone(
        taskId: String,
        done: Boolean,
        onResult: (Result<Unit>) -> Unit
    ) {
        val collection = tasksCollection()

        if (collection == null) {
            onResult(
                Result.failure(
                    IllegalStateException("Chưa đăng nhập Firebase")
                )
            )
            return
        }

        collection.document(taskId)
            .update("done", done)
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener { error ->
                onResult(Result.failure(error))
            }
    }


    // UPDATE: Sửa toàn bộ nội dung công việc
    fun updateTask(
        taskId: String,
        draft: TaskDraft,
        onResult: (Result<Unit>) -> Unit
    ) {
        val collection = tasksCollection()

        if (collection == null) {
            onResult(
                Result.failure(
                    IllegalStateException("Chưa đăng nhập Firebase")
                )
            )
            return
        }

        val updates = mapOf(
            "title" to draft.title.trim(),
            "note" to draft.note.trim(),
            "category" to draft.category.value,
            "date" to draft.date,
            "time" to draft.time,
            "remind" to draft.remind,
            "priority" to draft.priority.value
        )

        collection.document(taskId)
            .update(updates)
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener { error ->
                onResult(Result.failure(error))
            }
    }

    // DELETE: Xóa công việc khỏi Firestore
    fun deleteTask(
        taskId: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val collection = tasksCollection()

        if (collection == null) {
            onResult(
                Result.failure(
                    IllegalStateException("Chưa đăng nhập Firebase")
                )
            )
            return
        }

        collection.document(taskId)
            .delete()
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener { error ->
                onResult(Result.failure(error))
            }
    }
}
