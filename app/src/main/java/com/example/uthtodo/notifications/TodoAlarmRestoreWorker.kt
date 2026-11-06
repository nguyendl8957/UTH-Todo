package com.example.uthtodo.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.uthtodo.data.task.mapTaskSubtasks
import com.example.uthtodo.ui.screens.HomeTask
import com.example.uthtodo.ui.screens.HomeTaskCategory
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Restores only local alarms from the signed-in user's current Firestore tasks. */
class TodoAlarmRestoreWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        var user = auth.currentUser
        if (user == null) {
            val initialized = CountDownLatch(1)
            val listener = FirebaseAuth.AuthStateListener { initialized.countDown() }
            auth.addAuthStateListener(listener)
            try {
                initialized.await(10, TimeUnit.SECONDS)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                auth.removeAuthStateListener(listener)
                return Result.retry()
            }
            auth.removeAuthStateListener(listener)
            user = auth.currentUser
        }
        val uid = user?.uid ?: return Result.success()

        return try {
            val snapshot = Tasks.await(
                FirebaseFirestore.getInstance()
                    .collection("users").document(uid).collection("tasks")
                    .get(),
                25,
                TimeUnit.SECONDS
            )
            val tasks = snapshot.documents.mapNotNull { document ->
                val title = document.getString("title") ?: return@mapNotNull null
                val category = when (document.getString("category")) {
                    "study" -> HomeTaskCategory.STUDY
                    "life" -> HomeTaskCategory.LIFE
                    else -> return@mapNotNull null
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
                    subtasks = mapTaskSubtasks(
                        document.id,
                        document.get("subtasks") as? List<*>
                    ),
                    completedAt = document.getTimestamp("completedAt")?.toDate()
                )
            }
            TodoNotificationScheduler(applicationContext).sync(tasks)
            NotificationHistory.rescheduleExpirations(applicationContext, uid)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
