
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

private enum class StartScreen {
    SPLASH,
    ONBOARDING,
    LOGIN
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            UTHTodoTheme(darkTheme = false) {

                var currentScreen by rememberSaveable {
                    mutableStateOf(StartScreen.SPLASH)
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

                    StartScreen.LOGIN -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tiếp theo: Đăng nhập UTH Todo",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
