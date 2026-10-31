package com.example.uthtodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.uthtodo.ui.screens.SplashScreen
import com.example.uthtodo.ui.theme.UTHTodoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            UTHTodoTheme {
                var showSplash by remember {
                    mutableStateOf(true)
                }

                if (showSplash) {
                    SplashScreen(
                        onFinished = {
                            showSplash = false
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tiếp theo: Onboarding UTH Todo"
                        )
                    }
                }
            }
        }
    }
}
