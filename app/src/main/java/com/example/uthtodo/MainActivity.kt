package com.example.uthtodo

import android.os.Bundle
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import com.example.uthtodo.notifications.TodoNotificationScheduler
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.uthtodo.ui.theme.AppAppearance
import com.example.uthtodo.ui.theme.LocalizedContent
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.lifecycleScope
import com.example.uthtodo.data.auth.AuthRepository
import com.example.uthtodo.data.auth.GoogleSignInClient
import com.example.uthtodo.data.task.TaskRepository
import com.example.uthtodo.model.TaskCategory
import com.example.uthtodo.model.TaskDraft
import com.example.uthtodo.model.TaskPriority
import com.example.uthtodo.ui.screens.AuthTestScreen
import com.example.uthtodo.ui.screens.EmailAuthScreen
import com.example.uthtodo.ui.screens.HomeScreen
import com.example.uthtodo.ui.screens.HomeTask
import com.example.uthtodo.ui.screens.HomeTaskCategory
import com.example.uthtodo.ui.screens.LoginScreen
import com.example.uthtodo.ui.screens.OnboardingScreen
import com.example.uthtodo.ui.screens.SplashScreen
import com.example.uthtodo.ui.screens.TaskDetailScreen
import com.example.uthtodo.ui.screens.TaskEditorScreen
import com.example.uthtodo.ui.screens.NotificationsScreen
import com.example.uthtodo.ui.screens.ProfileScreen
import com.example.uthtodo.ui.screens.SettingsScreen
import com.example.uthtodo.ui.screens.TasksScreen
import com.example.uthtodo.ui.theme.UTHTodoTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

private enum class StartScreen {
    SPLASH,
    ONBOARDING,
    LOGIN,
    HOME,
    TASKS,
    TASK_EDITOR,
    TASK_DETAIL,
    SETTINGS,
    NOTIFICATIONS,
    PROFILE,
    AUTH_TEST,
    EMAIL_AUTH
}

class MainActivity : ComponentActivity() {

    private var googleSignInBusy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 204)
        }

        setContent {
            val appearance = remember { AppAppearance(applicationContext) }
            val systemDark = isSystemInDarkTheme()
            UTHTodoTheme(
                darkTheme = appearance.isDark(systemDark),
                accentColor = appearance.accentColor
            ) {
                LocalizedContent(appearance.languageTag) {

                    var currentScreen by rememberSaveable {
                        mutableStateOf(StartScreen.SPLASH)
                    }
                    var selectedTaskId by rememberSaveable {
                        mutableStateOf<String?>(null)
                    }


                    val tasks = remember {
                        mutableStateListOf<HomeTask>()
                    }

                    val taskRepository = remember {
                        TaskRepository()
                    }

                    val userId = FirebaseAuth.getInstance().currentUser?.uid

                    val isTaskArea = currentScreen == StartScreen.HOME ||
                            currentScreen == StartScreen.TASKS ||
                            currentScreen == StartScreen.TASK_EDITOR ||
                            currentScreen == StartScreen.TASK_DETAIL ||
                            currentScreen == StartScreen.SETTINGS ||
                            currentScreen == StartScreen.NOTIFICATIONS ||
                            currentScreen == StartScreen.PROFILE ||
                            currentScreen == StartScreen.AUTH_TEST

                    DisposableEffect(userId, isTaskArea) {
                        if (userId == null || !isTaskArea) {
                            tasks.clear()
                            onDispose { }
                        } else {
                            tasks.clear()

                            val listener = taskRepository.observeTasks { result ->
                                result.onSuccess { firebaseTasks ->
                                    tasks.clear()
                                    tasks.addAll(firebaseTasks)
                                    TodoNotificationScheduler(this@MainActivity).sync(firebaseTasks)
                                }

                                result.onFailure { error ->
                                    Toast.makeText(
                                        this@MainActivity,
                                        error.localizedMessage ?: "Không thể tải công việc",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                            onDispose {
                                listener?.remove()
                            }
                        }
                    }

                    var canResendVerification by rememberSaveable {
                        mutableStateOf(false)
                    }

                    when (currentScreen) {


                        StartScreen.SPLASH -> {
                            SplashScreen(
                                onFinished = {
                                    val currentUser =
                                        FirebaseAuth.getInstance().currentUser

                                    val preferences = getSharedPreferences(
                                        "uth_todo_startup",
                                        MODE_PRIVATE
                                    )

                                    val onboardingCompleted =
                                        preferences.getBoolean(
                                            "onboarding_completed",
                                            false
                                        )

                                    currentScreen = when {
                                        currentUser != null -> StartScreen.HOME
                                        onboardingCompleted -> StartScreen.LOGIN
                                        else -> StartScreen.ONBOARDING
                                    }
                                }
                            )
                        }


                        StartScreen.ONBOARDING -> {
                            OnboardingScreen(
                                onFinished = {
                                    getSharedPreferences(
                                        "uth_todo_startup",
                                        MODE_PRIVATE
                                    )
                                        .edit()
                                        .putBoolean("onboarding_completed", true)
                                        .apply()

                                    currentScreen = StartScreen.LOGIN
                                }
                            )
                        }


                        StartScreen.EMAIL_AUTH -> {
                            val repository = AuthRepository()

                            val showEmailNotReady = {
                                Toast.makeText(
                                    this@MainActivity,
                                    getString(R.string.login_not_ready),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            EmailAuthScreen(
                                canResendVerification = canResendVerification,
                                onBackClick = {
                                    canResendVerification = false
                                    currentScreen = StartScreen.LOGIN
                                },

                                onSignInClick = { email, password ->
                                    repository.signInWithEmail(
                                        email = email,
                                        password = password
                                    ) { result ->
                                        if (result.isSuccess) {
                                            repository.checkEmailVerified { verificationResult ->
                                                when {
                                                    verificationResult.isSuccess &&
                                                            verificationResult.getOrNull() == true -> {
                                                        canResendVerification = false
                                                        currentScreen = StartScreen.HOME
                                                    }

                                                    verificationResult.isSuccess -> {
                                                        canResendVerification = true

                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            getString(R.string.email_not_verified),
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }

                                                    else -> {
                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            getString(R.string.email_verification_check_failed),
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                }
                                            }
                                        } else {
                                            Toast.makeText(
                                                this@MainActivity,
                                                getString(R.string.email_login_failed),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },

                                onRegisterClick = { email, password ->
                                    repository.registerWithEmail(
                                        email = email,
                                        password = password
                                    ) { result ->
                                        if (result.isSuccess) {

                                            // Tài khoản mới đăng ký chưa xác minh email
                                            canResendVerification = true

                                            repository.sendVerificationEmail { verificationResult ->
                                                val message = if (verificationResult.isSuccess) {
                                                    getString(R.string.email_verification_sent)
                                                } else {
                                                    getString(R.string.email_verification_send_failed)
                                                }

                                                Toast.makeText(
                                                    this@MainActivity,
                                                    message,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                        else {
                                            Toast.makeText(
                                                this@MainActivity,
                                                getString(R.string.email_register_failed),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },

                                onForgotPasswordClick = { email ->
                                    if (email.isBlank()) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            getString(R.string.email_required),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        repository.sendPasswordResetEmail(email) { result ->
                                            val message = if (result.isSuccess) {
                                                getString(R.string.email_reset_requested)
                                            } else {
                                                getString(R.string.email_reset_failed)
                                            }

                                            Toast.makeText(
                                                this@MainActivity,
                                                message,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },

                                onResendVerificationClick = {
                                    repository.sendVerificationEmail { result ->
                                        val message = if (result.isSuccess) {
                                            getString(R.string.email_verification_sent_again)
                                        } else {
                                            getString(R.string.email_verification_send_failed)
                                        }

                                        Toast.makeText(
                                            this@MainActivity,
                                            message,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            )
                        }

                        StartScreen.HOME -> {
                            HomeScreen(
                                user = AuthRepository().currentUser(),
                                tasks = tasks,
                                onAvatarClick = {
                                    currentScreen = StartScreen.PROFILE
                                },
                                onViewAllClick = {
                                    currentScreen = StartScreen.TASKS
                                },
                                onStudyClick = {
                                    currentScreen = StartScreen.TASKS
                                },
                                onAddClick = {
                                    selectedTaskId = null
                                    currentScreen = StartScreen.TASK_EDITOR
                                },
                                onTaskClick = { taskId ->
                                    selectedTaskId = taskId
                                    currentScreen = StartScreen.TASK_DETAIL
                                },

                                onTaskCheckedChange = { taskId, completed ->
                                    taskRepository.updateTaskDone(
                                        taskId = taskId,
                                        done = completed
                                    ) { result ->
                                        result.onFailure { error ->
                                            Toast.makeText(
                                                this@MainActivity,
                                                error.localizedMessage
                                                    ?: getString(R.string.task_update_failed),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                onLifeClick = {
                                    currentScreen = StartScreen.TASKS
                                },
                                onNotificationsClick = {
                                    currentScreen = StartScreen.NOTIFICATIONS
                                },
                                onSettingsClick = {
                                    currentScreen = StartScreen.SETTINGS
                                }
                            )
                        }

                        StartScreen.TASKS -> {
                            TasksScreen(
                                tasks = tasks,
                                onHomeClick = {
                                    currentScreen = StartScreen.HOME
                                },
                                onAddClick = {
                                    selectedTaskId = null
                                    currentScreen = StartScreen.TASK_EDITOR
                                },
                                onNotificationsClick = {
                                    currentScreen = StartScreen.NOTIFICATIONS
                                },
                                onSettingsClick = {
                                    currentScreen = StartScreen.SETTINGS
                                },
                                onTaskClick = { taskId ->
                                    selectedTaskId = taskId
                                    currentScreen = StartScreen.TASK_DETAIL
                                },


                                onTaskCheckedChange = { taskId, completed ->
                                    taskRepository.updateTaskDone(
                                        taskId = taskId,
                                        done = completed
                                    ) { result ->
                                        result.onFailure { error ->
                                            Toast.makeText(
                                                this@MainActivity,
                                                error.localizedMessage ?: "Không thể cập nhật công việc",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            )
                        }


                        StartScreen.SETTINGS -> {
                            SettingsScreen(
                                onHomeClick = { currentScreen = StartScreen.HOME },
                                onTasksClick = { currentScreen = StartScreen.TASKS },
                                onProfileClick = { currentScreen = StartScreen.PROFILE },
                                onAddClick = {
                                    selectedTaskId = null
                                    currentScreen = StartScreen.TASK_EDITOR
                                },
                                onNotificationsClick = {
                                    currentScreen = StartScreen.NOTIFICATIONS
                                },
                                appearance = appearance,
                                tasksForReminders = tasks
                            )
                        }

                        StartScreen.NOTIFICATIONS -> {
                            NotificationsScreen(
                                onHomeClick = { currentScreen = StartScreen.HOME },
                                onTasksClick = { currentScreen = StartScreen.TASKS },
                                onAddClick = {
                                    selectedTaskId = null
                                    currentScreen = StartScreen.TASK_EDITOR
                                },
                                onSettingsClick = { currentScreen = StartScreen.SETTINGS }
                            )
                        }

                        StartScreen.PROFILE -> {
                            ProfileScreen(
                                user = AuthRepository().currentUser(),
                                tasks = tasks,
                                onBack = { currentScreen = StartScreen.SETTINGS },
                                onSignOut = {
                                    lifecycleScope.launch {
                                        try {
                                            CredentialManager.create(this@MainActivity)
                                                .clearCredentialState(ClearCredentialStateRequest())
                                        } catch (_: Exception) {
                                            // Firebase signOut vẫn thực hiện nếu không dọn được Credential Manager.
                                        }
                                        TodoNotificationScheduler(this@MainActivity).sync(emptyList())
                                        AuthRepository().signOut()
                                        googleSignInBusy = false
                                        canResendVerification = false
                                        selectedTaskId = null
                                        tasks.clear()
                                        currentScreen = StartScreen.LOGIN
                                    }
                                }
                            )
                        }

                        StartScreen.TASK_DETAIL -> {
                            val selectedTask = tasks.firstOrNull {
                                it.id == selectedTaskId
                            }

                            if (selectedTask == null) {
                                Text(
                                    text = stringResource(R.string.detail_loading),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .wrapContentSize(),
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                TaskDetailScreen(
                                    task = selectedTask,
                                    onBack = {
                                        currentScreen = StartScreen.TASKS
                                    },
                                    onEdit = {
                                        currentScreen = StartScreen.TASK_EDITOR
                                    },
                                    onToggleDone = {
                                        taskRepository.updateTaskDone(
                                            taskId = selectedTask.id,
                                            done = !selectedTask.done
                                        ) { result ->
                                            result.onFailure { error ->
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    error.localizedMessage
                                                        ?: getString(
                                                            R.string.detail_operation_failed
                                                        ),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    },
                                    onDelete = {
                                        taskRepository.deleteTask(
                                            taskId = selectedTask.id
                                        ) { result ->
                                            result.onSuccess {
                                                selectedTaskId = null
                                                currentScreen = StartScreen.TASKS
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    getString(R.string.detail_deleted),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                            result.onFailure { error ->
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    error.localizedMessage
                                                        ?: getString(
                                                            R.string.detail_operation_failed
                                                        ),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    },
                                    onSubtasksChange = { updatedSubtasks ->
                                        taskRepository.updateSubtasks(
                                            taskId = selectedTask.id,
                                            subtasks = updatedSubtasks
                                        ) { result ->
                                            result.onFailure { error ->
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    error.localizedMessage ?: getString(R.string.uth_subtasks_save_failed),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }


                        StartScreen.TASK_EDITOR -> {
                            val taskToEdit = tasks.firstOrNull {
                                it.id == selectedTaskId
                            }

                            val initialDraft = taskToEdit?.let { task ->
                                TaskDraft(
                                    title = task.title,
                                    note = task.note,
                                    category = when (task.category) {
                                        HomeTaskCategory.STUDY -> TaskCategory.STUDY
                                        HomeTaskCategory.LIFE -> TaskCategory.LIFE
                                    },
                                    date = task.date,
                                    time = task.time,
                                    remind = task.remind,
                                    priority = when (task.priority) {
                                        "low" -> TaskPriority.LOW
                                        "high" -> TaskPriority.HIGH
                                        else -> TaskPriority.MEDIUM
                                    }
                                )
                            }

                            TaskEditorScreen(
                                initialDraft = initialDraft,
                                onCancel = {
                                    currentScreen = if (taskToEdit != null) {
                                        StartScreen.TASK_DETAIL
                                    } else {
                                        StartScreen.TASKS
                                    }
                                },
                                onSave = { draft ->
                                    if (taskToEdit == null) {
                                        taskRepository.addTask(draft) { result ->
                                            result.onSuccess {
                                                selectedTaskId = null
                                                currentScreen = StartScreen.TASKS
                                            }

                                            result.onFailure { error ->
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    error.localizedMessage
                                                        ?: getString(
                                                            R.string.detail_operation_failed
                                                        ),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    } else {
                                        taskRepository.updateTask(
                                            taskId = taskToEdit.id,
                                            draft = draft
                                        ) { result ->
                                            result.onSuccess {
                                                currentScreen = StartScreen.TASK_DETAIL
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    getString(R.string.detail_saved),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                            result.onFailure { error ->
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    error.localizedMessage
                                                        ?: getString(
                                                            R.string.detail_operation_failed
                                                        ),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        StartScreen.AUTH_TEST -> {
                            AuthTestScreen(
                                onSignOutClick = {
                                    lifecycleScope.launch {
                                        try {
                                            CredentialManager
                                                .create(this@MainActivity)
                                                .clearCredentialState(
                                                    ClearCredentialStateRequest()
                                                )
                                        } catch (e: Exception) {
                                            // Lỗi dọn trạng thái Credential Manager
                                            // không có nghĩa Firebase đã đăng xuất.
                                        }

                                        TodoNotificationScheduler(this@MainActivity).sync(emptyList())
                                        AuthRepository().signOut()
                                        googleSignInBusy = false
                                        canResendVerification = false
                                        currentScreen = StartScreen.LOGIN
                                    }
                                }
                            )
                        }


                        StartScreen.LOGIN -> {
                            val showNotReady = {
                                Toast.makeText(
                                    this@MainActivity,
                                    getString(R.string.login_not_ready),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            LoginScreen(
                                onGoogleClick = {
                                    if (!googleSignInBusy) {
                                        googleSignInBusy = true

                                        lifecycleScope.launch {
                                            try {
                                                val googleClient =
                                                    GoogleSignInClient(this@MainActivity)

                                                val credential =
                                                    googleClient.getFirebaseCredential()

                                                val repository = AuthRepository()

                                                repository.signInWithCredential(credential) { result ->
                                                    googleSignInBusy = false

                                                    val message: String = if (result.isSuccess) {
                                                        currentScreen = StartScreen.HOME
                                                        getString(R.string.login_google_success)
                                                    } else {
                                                        getString(R.string.login_google_failed)
                                                    }

                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        message,
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            } catch (e: GetCredentialCancellationException) {
                                                // Người dùng hủy chọn Google: trở lại Login.
                                                googleSignInBusy = false
                                            } catch (e: Exception) {
                                                googleSignInBusy = false

                                                Toast.makeText(
                                                    this@MainActivity,
                                                    getString(R.string.login_google_failed),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                },
                                onEmailClick = {
                                    currentScreen = StartScreen.EMAIL_AUTH
                                },
                                onGithubClick = {
                                    val repository = AuthRepository()

                                    repository.signInWithGitHub(
                                        activity = this@MainActivity
                                    ) { result ->
                                        if (result.isSuccess) {
                                            googleSignInBusy = false
                                            currentScreen = StartScreen.HOME

                                            Toast.makeText(
                                                this@MainActivity,
                                                getString(R.string.github_login_success),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@MainActivity,
                                                getString(R.string.github_login_failed),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },

                                onGuestClick = {
                                    val repository = AuthRepository()
                                    val existingUser = repository.currentUser()

                                    if (existingUser != null && !existingUser.isAnonymous) {
                                        // Đang có tài khoản Google/Email:
                                        // không âm thầm thay thế phiên đăng nhập.
                                        Toast.makeText(
                                            this@MainActivity,
                                            getString(R.string.guest_existing_account),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        repository.signInAnonymously { result ->
                                            if (result.isSuccess) {
                                                currentScreen = StartScreen.HOME
                                            } else {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    getString(R.string.guest_login_failed),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}