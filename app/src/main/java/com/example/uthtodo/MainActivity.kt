package com.example.uthtodo

import android.os.Bundle
import android.Manifest
import android.content.SharedPreferences
import android.content.res.Configuration
import com.example.uthtodo.notifications.TodoNotificationScheduler
import com.example.uthtodo.notifications.NotificationHistory
import com.example.uthtodo.notifications.TodoNotificationEntry
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.uthtodo.ui.theme.AppAppearance
import com.example.uthtodo.ui.theme.LocalizedContent
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.edit
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
import com.example.uthtodo.ui.screens.TaskFilter
import com.example.uthtodo.ui.theme.UTHTodoTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Locale

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
    private var openedNotificationId by mutableStateOf<String?>(null)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openedNotificationId = intent.getStringExtra("open_notification_id")
    }

    private var googleSignInBusy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openedNotificationId = intent?.getStringExtra("open_notification_id")

        enableEdgeToEdge()
        setContent {
            val appearance = remember { AppAppearance(applicationContext) }
            val systemDark = isSystemInDarkTheme()
            UTHTodoTheme(
                darkTheme = appearance.isDark(systemDark),
                accentColor = appearance.accentColor
            ) {
                var pendingNotificationPermissionResult by remember {
                    mutableStateOf<((Boolean) -> Unit)?>(null)
                }
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { granted ->
                    pendingNotificationPermissionResult?.invoke(granted)
                    pendingNotificationPermissionResult = null
                }
                LocalizedContent(appearance.languageTag) {
                    val localizedResources = remember(appearance.languageTag) {
                        val config = Configuration(resources.configuration).apply {
                            setLocale(Locale.forLanguageTag(appearance.languageTag))
                        }
                        createConfigurationContext(config).resources
                    }
                    fun getString(resourceId: Int): String = localizedResources.getString(resourceId)
                    val completionTitle = stringResource(R.string.uth_notification_type_completion)
                    val completionMessageFormat = stringResource(R.string.uth_notification_task_completed)

                    var currentScreen by rememberSaveable {
                        mutableStateOf(StartScreen.SPLASH)
                    }
                    var selectedTaskId by rememberSaveable {
                        mutableStateOf<String?>(null)
                    }
                    var taskFilterOnEntry by remember {
                        mutableStateOf(TaskFilter.ALL)
                    }

                    val backStack = remember { mutableStateListOf<StartScreen>() }
                    val navigateTo: (StartScreen) -> Unit = { destination ->
                        if (destination != currentScreen) {
                            backStack.add(currentScreen)
                            currentScreen = destination
                        }
                    }
                    val replaceCurrentScreen: (StartScreen) -> Unit = { destination ->
                        if (backStack.lastOrNull() == destination) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                        currentScreen = destination
                    }
                    val resetNavigation: (StartScreen) -> Unit = { destination ->
                        backStack.clear()
                        currentScreen = destination
                    }
                    val navigateBack: () -> Unit = {
                        if (backStack.isNotEmpty()) {
                            currentScreen = backStack.removeAt(backStack.lastIndex)
                        }
                    }
                    BackHandler(enabled = backStack.isNotEmpty()) {
                        navigateBack()
                    }


                    val tasks = remember {
                        mutableStateListOf<HomeTask>()
                    }

                    val requestNotificationPermission: ((Boolean) -> Unit) -> Unit = { onResult ->
                        pendingNotificationPermissionResult = onResult
                        notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }

                    val taskRepository = remember {
                        TaskRepository()
                    }

                    val userId = FirebaseAuth.getInstance().currentUser?.uid

                    val recordTaskCompletion: (String, String) -> Unit = { taskId, title ->
                        userId?.let { uid ->
                            NotificationHistory.add(
                                applicationContext,
                                uid,
                                TodoNotificationEntry(
                                    title = completionTitle,
                                    message = String.format(
                                        Locale.forLanguageTag(appearance.languageTag),
                                        completionMessageFormat,
                                        title
                                    ),
                                    timestamp = System.currentTimeMillis(),
                                    type = com.example.uthtodo.notifications.TodoNotificationType.COMPLETION,
                                    taskTitle = title
                                )
                            )
                        }
                    }

                    val notificationEntries = remember(userId) {
                        mutableStateListOf<TodoNotificationEntry>().apply {
                            userId?.let { addAll(NotificationHistory.read(applicationContext, it)) }
                        }
                    }

                    LaunchedEffect(openedNotificationId, userId) {
                        val id = openedNotificationId
                        val uid = userId
                        if (id != null && uid != null) {
                            NotificationHistory.markRead(applicationContext, uid, id)
                            resetNavigation(StartScreen.NOTIFICATIONS)
                            openedNotificationId = null
                        }
                    }

                    DisposableEffect(userId, notificationEntries) {
                        if (userId == null) {
                            onDispose { }
                        } else {
                            val preferences = getSharedPreferences(
                                "uth_notification_history", MODE_PRIVATE
                            )
                            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                                if (key == "notifications_$userId") {
                                    val stored = NotificationHistory.read(applicationContext, userId)
                                    notificationEntries.clear()
                                    notificationEntries.addAll(stored)
                                }
                            }
                            preferences.registerOnSharedPreferenceChangeListener(listener)
                            onDispose {
                                preferences.unregisterOnSharedPreferenceChangeListener(listener)
                            }
                        }
                    }

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

                                    resetNavigation(when {
                                        currentUser != null -> StartScreen.HOME
                                        onboardingCompleted -> StartScreen.LOGIN
                                        else -> StartScreen.ONBOARDING
                                    })
                                }
                            )
                        }


                        StartScreen.ONBOARDING -> {
                            OnboardingScreen(
                                onFinished = {
                                    getSharedPreferences(
                                        "uth_todo_startup",
                                        MODE_PRIVATE
                                    ).edit {
                                        putBoolean("onboarding_completed", true)
                                    }

                                    navigateTo(StartScreen.LOGIN)
                                }
                            )
                        }


                        StartScreen.EMAIL_AUTH -> {
                            val repository = AuthRepository()

                            EmailAuthScreen(
                                canResendVerification = canResendVerification,
                                onBackClick = {
                                    canResendVerification = false
                                    navigateBack()
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
                                                        resetNavigation(StartScreen.HOME)
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
                                hasUnreadNotifications = notificationEntries.any { !it.isRead },
                                onAvatarClick = {
                                    navigateTo(StartScreen.PROFILE)
                                },
                                onViewAllClick = {
                                    taskFilterOnEntry = TaskFilter.ALL
                                    navigateTo(StartScreen.TASKS)
                                },
                                onStudyClick = {
                                    taskFilterOnEntry = TaskFilter.STUDY
                                    navigateTo(StartScreen.TASKS)
                                },
                                onAddClick = {
                                    selectedTaskId = null
                                    navigateTo(StartScreen.TASK_EDITOR)
                                },
                                onTaskClick = { taskId ->
                                    selectedTaskId = taskId
                                    navigateTo(StartScreen.TASK_DETAIL)
                                },

                                onTaskCheckedChange = { taskId, completed ->
                                    val previousTask = tasks.firstOrNull { it.id == taskId }
                                    taskRepository.updateTaskDone(
                                        taskId = taskId,
                                        done = completed
                                    ) { result ->
                                        result.onSuccess { changed ->
                                            if (changed && completed && previousTask?.done == false) {
                                                recordTaskCompletion(previousTask.id, previousTask.title)
                                            }
                                        }
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
                                    taskFilterOnEntry = TaskFilter.LIFE
                                    navigateTo(StartScreen.TASKS)
                                },
                                onNotificationsClick = {
                                    navigateTo(StartScreen.NOTIFICATIONS)
                                },
                                onSettingsClick = {
                                    navigateTo(StartScreen.SETTINGS)
                                }
                            )
                        }

                        StartScreen.TASKS -> {
                            TasksScreen(
                                tasks = tasks,
                                initialFilter = taskFilterOnEntry,
                                onHomeClick = {
                                    resetNavigation(StartScreen.HOME)
                                },
                                onAddClick = {
                                    selectedTaskId = null
                                    navigateTo(StartScreen.TASK_EDITOR)
                                },
                                onNotificationsClick = {
                                    navigateTo(StartScreen.NOTIFICATIONS)
                                },
                                onSettingsClick = {
                                    navigateTo(StartScreen.SETTINGS)
                                },
                                onTaskClick = { taskId ->
                                    selectedTaskId = taskId
                                    navigateTo(StartScreen.TASK_DETAIL)
                                },


                                onTaskCheckedChange = { taskId, completed ->
                                    val previousTask = tasks.firstOrNull { it.id == taskId }
                                    taskRepository.updateTaskDone(
                                        taskId = taskId,
                                        done = completed
                                    ) { result ->
                                        result.onSuccess { changed ->
                                            if (changed && completed && previousTask?.done == false) {
                                                recordTaskCompletion(previousTask.id, previousTask.title)
                                            }
                                        }
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
                                onHomeClick = { resetNavigation(StartScreen.HOME) },
                                onTasksClick = {
                                    taskFilterOnEntry = TaskFilter.ALL
                                    navigateTo(StartScreen.TASKS)
                                },
                                onProfileClick = { navigateTo(StartScreen.PROFILE) },
                                onAddClick = {
                                    selectedTaskId = null
                                    navigateTo(StartScreen.TASK_EDITOR)
                                },
                                onNotificationsClick = {
                                    navigateTo(StartScreen.NOTIFICATIONS)
                                },
                                appearance = appearance,
                                user = AuthRepository().currentUser(),
                                tasksForReminders = tasks,
                                onRequestNotificationPermission = requestNotificationPermission
                            )
                        }

                        StartScreen.NOTIFICATIONS -> {
                            NotificationsScreen(
                                entries = notificationEntries,
                                onReadNotification = { id ->
                                    userId?.let { uid ->
                                        NotificationHistory.markRead(this@MainActivity, uid, id)
                                    }
                                },
                                onMarkAllRead = {
                                    userId?.let { uid ->
                                        NotificationHistory.markAllRead(this@MainActivity, uid)
                                    }
                                },
                                onHomeClick = { resetNavigation(StartScreen.HOME) },
                                onTasksClick = {
                                    taskFilterOnEntry = TaskFilter.ALL
                                    navigateTo(StartScreen.TASKS)
                                },
                                onAddClick = {
                                    selectedTaskId = null
                                    navigateTo(StartScreen.TASK_EDITOR)
                                },
                                onSettingsClick = { navigateTo(StartScreen.SETTINGS) }
                            )
                        }

                        StartScreen.PROFILE -> {
                            ProfileScreen(
                                user = AuthRepository().currentUser(),
                                tasks = tasks,
                                onBack = { navigateBack() },
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
                                        getSharedPreferences("uth_todo_startup", MODE_PRIVATE)
                                            .edit { putBoolean("onboarding_completed", false) }
                                        resetNavigation(StartScreen.LOGIN)
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
                                        navigateBack()
                                    },
                                    onEdit = {
                                        navigateTo(StartScreen.TASK_EDITOR)
                                    },
                                    onToggleDone = {
                                        taskRepository.updateTaskDone(
                                            taskId = selectedTask.id,
                                            done = !selectedTask.done
                                        ) { result ->
                                            result.onSuccess { changed ->
                                                if (changed && !selectedTask.done) {
                                                    recordTaskCompletion(selectedTask.id, selectedTask.title)
                                                }
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
                                    onDelete = {
                                        taskRepository.deleteTask(
                                            taskId = selectedTask.id
                                        ) { result ->
                                            result.onSuccess {
                                                selectedTaskId = null
                                            navigateBack()
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
                                    navigateBack()
                                },
                                onSave = { draft ->
                                    if (taskToEdit == null) {
                                        taskRepository.addTask(draft) { result ->
                                            result.onSuccess { createdTaskId ->
                                                selectedTaskId = createdTaskId
                                                replaceCurrentScreen(StartScreen.TASK_DETAIL)
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
                                                navigateBack()
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
                                        } catch (_: Exception) {
                                            // Lỗi dọn trạng thái Credential Manager
                                            // không có nghĩa Firebase đã đăng xuất.
                                        }

                                        TodoNotificationScheduler(this@MainActivity).sync(emptyList())
                                        AuthRepository().signOut()
                                        googleSignInBusy = false
                                        canResendVerification = false
                                        getSharedPreferences("uth_todo_startup", MODE_PRIVATE)
                                            .edit { putBoolean("onboarding_completed", false) }
                                        resetNavigation(StartScreen.LOGIN)
                                    }
                                }
                            )
                        }


                        StartScreen.LOGIN -> {
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
                                                        resetNavigation(StartScreen.HOME)
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
                                            } catch (_: GetCredentialCancellationException) {
                                                // Người dùng hủy chọn Google: trở lại Login.
                                                googleSignInBusy = false
                                            } catch (_: Exception) {
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
                                    navigateTo(StartScreen.EMAIL_AUTH)
                                },
                                onGithubClick = {
                                    val repository = AuthRepository()

                                    repository.signInWithGitHub(
                                        activity = this@MainActivity
                                    ) { result ->
                                        if (result.isSuccess) {
                                            googleSignInBusy = false
                                            resetNavigation(StartScreen.HOME)

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
                                                resetNavigation(StartScreen.HOME)
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
