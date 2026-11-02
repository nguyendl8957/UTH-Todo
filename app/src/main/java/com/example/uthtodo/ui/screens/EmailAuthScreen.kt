package com.example.uthtodo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.res.stringResource
import com.example.uthtodo.R

import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@Composable
fun EmailAuthScreen(
    canResendVerification: Boolean,
    onBackClick: () -> Unit,
    onSignInClick: (String, String) -> Unit,
    onRegisterClick: (String, String) -> Unit,
    onResendVerificationClick: () -> Unit,
    onForgotPasswordClick: (String) -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var registerMode by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBackClick) {
            Text("← Quay lại")
        }

        Text(
            text = if (registerMode) "Đăng ký tài khoản"
            else "Đăng nhập Email",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            singleLine = true,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                    }
                ) {
                    Icon(
                        painter = painterResource(
                            if (passwordVisible) {
                                R.drawable.ic_visibility_off
                            } else {
                                R.drawable.ic_visibility
                            }
                        ),
                        contentDescription = stringResource(
                            if (passwordVisible) {
                                R.string.email_hide_password
                            } else {
                                R.string.email_show_password
                            }
                        ),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (registerMode) {
                    onRegisterClick(email.trim(), password)
                } else {
                    onSignInClick(email.trim(), password)
                }
            },
            enabled = email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            Text(if (registerMode) "Đăng ký" else "Đăng nhập")
        }

        TextButton(
            onClick = { registerMode = !registerMode },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                if (registerMode) "Đã có tài khoản? Đăng nhập"
                else "Chưa có tài khoản? Đăng ký"
            )
        }

        if (!registerMode) {
            if (canResendVerification) {
                TextButton(
                    onClick = onResendVerificationClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(stringResource(R.string.email_resend_verification))
                }
            }

            TextButton(
                onClick = { onForgotPasswordClick(email.trim()) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Quên mật khẩu?")
            }
        }
    }
}
