package com.example.uthtodo.data.task

import com.example.uthtodo.model.TaskDraft
import com.example.uthtodo.ui.screens.HomeSubtask
import com.example.uthtodo.ui.screens.HomeTask
import com.example.uthtodo.ui.screens.HomeTaskCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.UUID

class TaskRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun tasksCollection(): CollectionReference? = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("tasks")
    }

    private fun notSignedIn() = IllegalStateException("Chưa đăng nhập Firebase")

    fun addTask(draft: TaskDraft, onResult: (Result<String>) -> Unit) {
        val collection = tasksCollection() ?: run {
            onResult(Result.failure(notSignedIn())); return
        }
        val doc = collection.document()
        val data = hashMapOf(
            "title" to draft.title.trim(),
            "note" to draft.note.trim(),
            "category" to draft.category.value,
            "date" to draft.date,
            "time" to draft.time,
            "remind" to draft.remind,
            "priority" to draft.priority.value,
            "done" to false,
            "subtasks" to emptyList<Map<String, Any>>(),
            "createdAt" to FieldValue.serverTimestamp()
        )
        doc.set(data).addOnSuccessListener { onResult(Result.success(doc.id)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun observeTasks(onResult: (Result<List<HomeTask>>) -> Unit): ListenerRegistration? {
        val collection = tasksCollection() ?: run {
            onResult(Result.failure(notSignedIn())); return null
        }
        return collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onResult(Result.failure(error)); return@addSnapshotListener
            }
            if (snapshot == null) {
                onResult(Result.failure(IllegalStateException("Không nhận được dữ liệu")))
                return@addSnapshotListener
            }
            val tasks = snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val category = when (doc.getString("category")) {
                    "study" -> HomeTaskCategory.STUDY
                    "life" -> HomeTaskCategory.LIFE
                    else -> return@mapNotNull null
                }
                val subtasks = (doc.get("subtasks") as? List<*>).orEmpty().mapNotNull { raw ->
                    val map = raw as? Map<*, *> ?: return@mapNotNull null
                    val id = map["id"] as? String ?: return@mapNotNull null
                    val subtitle = map["title"] as? String ?: return@mapNotNull null
                    HomeSubtask(id, subtitle, map["done"] as? Boolean ?: false)
                }
                HomeTask(
                    id = doc.id,
                    title = title,
                    category = category,
                    date = doc.getString("date").orEmpty(),
                    time = doc.getString("time").orEmpty(),
                    done = doc.getBoolean("done") ?: false,
                    note = doc.getString("note").orEmpty(),
                    priority = doc.getString("priority") ?: "medium",
                    remind = doc.getBoolean("remind") ?: true,
                    subtasks = subtasks,
                    completedAt = doc.getTimestamp("completedAt")?.toDate()
                )
            }
            onResult(Result.success(tasks))
        }
    }

    fun updateTaskDone(taskId: String, done: Boolean, onResult: (Result<Unit>) -> Unit) {
        val collection = tasksCollection() ?: run {
            onResult(Result.failure(notSignedIn())); return
        }
        val fields: Map<String, Any> = mapOf(
            "done" to done,
            "completedAt" to if (done) FieldValue.serverTimestamp() else FieldValue.delete()
        )
        collection.document(taskId).update(fields)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun updateTask(taskId: String, draft: TaskDraft, onResult: (Result<Unit>) -> Unit) {
        val collection = tasksCollection() ?: run {
            onResult(Result.failure(notSignedIn())); return
        }
        val fields = mapOf(
            "title" to draft.title.trim(),
            "note" to draft.note.trim(),
            "category" to draft.category.value,
            "date" to draft.date,
            "time" to draft.time,
            "remind" to draft.remind,
            "priority" to draft.priority.value
        )
        collection.document(taskId).update(fields)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun deleteTask(taskId: String, onResult: (Result<Unit>) -> Unit) {
        val collection = tasksCollection() ?: run {
            onResult(Result.failure(notSignedIn())); return
        }
        collection.document(taskId).delete()
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun updateSubtasks(taskId: String, subtasks: List<HomeSubtask>, onResult: (Result<Unit>) -> Unit) {
        val collection = tasksCollection() ?: run {
            onResult(Result.failure(notSignedIn())); return
        }
        val data = subtasks.map { mapOf(
            "id" to it.id,
            "title" to it.title,
            "done" to it.done
        ) }
        collection.document(taskId).update("subtasks", data)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun addSubtask(
        taskId: String,
        currentSubtasks: List<HomeSubtask>,
        title: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val trimmed = title.trim()
        if (trimmed.isBlank()) {
            onResult(Result.failure(IllegalArgumentException("Tên việc nhỏ không được để trống")))
            return
        }
        updateSubtasks(taskId, currentSubtasks + HomeSubtask(UUID.randomUUID().toString(), trimmed), onResult)
    }
}