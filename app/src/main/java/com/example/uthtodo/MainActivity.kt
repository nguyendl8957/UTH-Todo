package com.example.uthtodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import com.example.uthtodo.ui.screens.OnboardingScreen
import com.example.uthtodo.ui.screens.SplashScreen
import com.example.uthtodo.ui.theme.UTHTodoTheme
import androidx.compose.ui.res.stringResource

import android.widget.Toast
import com.example.uthtodo.ui.screens.LoginScreen

import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.lifecycleScope
import com.example.uthtodo.data.auth.AuthRepository
import com.example.uthtodo.data.auth.GoogleSignInClient
import kotlinx.coroutines.launch

import com.example.uthtodo.ui.screens.AuthTestScreen
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest

import com.example.uthtodo.ui.screens.EmailAuthScreen

import com.example.uthtodo.ui.screens.HomeScreen

import com.example.uthtodo.ui.screens.TasksScreen
import com.example.uthtodo.ui.screens.TaskFilter

import com.example.uthtodo.ui.screens.TaskEditorScreen

private enum class StartScreen {
    SPLASH,
    ONBOARDING,
    LOGIN,
    HOME,
    TASKS,
    TASK_EDITOR,
    AUTH_TEST,
    EMAIL_AUTH
}

class MainActivity : ComponentActivity() {

    private var googleSignInBusy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            UTHTodoTheme(darkTheme = false) {

                var currentScreen by rememberSaveable {
                    mutableStateOf(StartScreen.SPLASH)
                }
                var canResendVerification by rememberSaveable {
                    mutableStateOf(false)
                }

                when (currentScreen) {

                    StartScreen.SPLASH -> {
                        SplashScreen(
                            onFinished = {
                                currentScreen = StartScreen.ONBOARDING
                            }
                        )
                    }

                    StartScreen.ONBOARDING -> {
                        OnboardingScreen(
                            onFinished = {
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
                            onAvatarClick = {
                                currentScreen = StartScreen.AUTH_TEST
                            },
                            onViewAllClick = {
                                currentScreen = StartScreen.TASKS
                            },
                            onStudyClick = {
                                currentScreen = StartScreen.TASKS
                            },
                            onAddClick = {
                                currentScreen = StartScreen.TASK_EDITOR
                            },
                            onLifeClick = {
                                currentScreen = StartScreen.TASKS
                            }
                        )
                    }

                    StartScreen.TASKS -> {
                        TasksScreen(
                            tasks = emptyList(),
                            onHomeClick = {
                                currentScreen = StartScreen.HOME
                            },
                            onAddClick = {
                                currentScreen = StartScreen.TASK_EDITOR
                            },
                            onNotificationsClick = {
                                // Nối màn Thông báo sau.
                            },
                            onSettingsClick = {
                                // Nối màn Cài đặt sau.
                            },
                            onTaskClick = { _ ->
                                // Nối màn Chi tiết công việc sau.
                            },
                            onTaskCheckedChange = { _, _ ->
                                // Nối Repository khi có dữ liệu.
                            }
                        )
                    }

                    StartScreen.TASK_EDITOR -> {
                        TaskEditorScreen(
                            onCancel = {
                                currentScreen = StartScreen.TASKS
                            },
                            onSave = { draft ->
                                // Bước tiếp theo: lưu draft vào Repository/Firestore.
                                // Hiện chưa báo thành công vì chưa lưu dữ liệu.
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
