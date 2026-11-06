package com.example.uthtodo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
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
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBackClick) {
            Text(stringResource(R.string.email_screen_back))
        }

        Text(
            text = stringResource(
                if (registerMode) R.string.email_screen_register_title
                else R.string.email_screen_sign_in_title
            ),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email_screen_email_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.onSurface,
                unfocusedTextColor = colors.onSurface,
                focusedLabelColor = colors.primary,
                unfocusedLabelColor = colors.onSurfaceVariant,
                cursorColor = colors.primary,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outline
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.email_screen_password_label)) },
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.onSurface,
                unfocusedTextColor = colors.onSurface,
                focusedLabelColor = colors.primary,
                unfocusedLabelColor = colors.onSurfaceVariant,
                cursorColor = colors.primary,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outline
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
            Text(
                stringResource(
                    if (registerMode) R.string.email_screen_register_action
                    else R.string.email_screen_sign_in_action
                )
            )
        }

        TextButton(
            onClick = { registerMode = !registerMode },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                stringResource(
                    if (registerMode) R.string.email_screen_existing_account
                    else R.string.email_screen_new_account
                )
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
                Text(stringResource(R.string.email_screen_forgot_password))
            }
        }
    }
}
