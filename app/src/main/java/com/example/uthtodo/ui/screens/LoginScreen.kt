
package com.example.uthtodo.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uthtodo.R
import com.example.uthtodo.ui.theme.UTHColors

@Composable
fun LoginScreen(
    onGoogleClick: () -> Unit,
    onEmailClick: () -> Unit,
    onGithubClick: () -> Unit,
    onGuestClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Chiều cao khả dụng sau khi trừ các thanh hệ thống.
        val screenHeight = maxHeight

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = screenHeight)
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 44.dp,
                    bottom = 24.dp
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // CỤM TRÊN: logo, tiêu đề và mô tả.
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .background(
                            color = UTHColors.Primary,
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            R.drawable.ic_onboarding_1_1
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    text = stringResource(R.string.login_welcome),
                    fontSize = 30.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onBackground
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.login_description),
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = colors.onSurfaceVariant
                )
            }

            // Khoảng cách tối thiểu giữa hai cụm.
            Spacer(Modifier.height(36.dp))

            // CỤM DƯỚI: các lựa chọn đăng nhập.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoginProviderButton(
                    text = stringResource(R.string.login_google),
                    iconRes = R.drawable.ic_login_google,
                    preserveIconColors = true,
                    onClick = onGoogleClick
                )

                Spacer(Modifier.height(14.dp))

                LoginProviderButton(
                    text = stringResource(R.string.login_email),
                    iconRes = R.drawable.ic_login_email,
                    onClick = onEmailClick
                )

                Spacer(Modifier.height(14.dp))

                LoginProviderButton(
                    text = stringResource(R.string.login_github),
                    iconRes = R.drawable.ic_login_github,
                    onClick = onGithubClick
                )

                Spacer(Modifier.height(26.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = stringResource(R.string.login_or),
                        color = colors.onSurfaceVariant
                    )

                    HorizontalDivider(
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(22.dp))

                Text(
                    text = stringResource(R.string.login_guest),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onGuestClick)
                        .padding(12.dp),
                    textAlign = TextAlign.Center,
                    color = colors.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.login_terms),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LoginProviderButton(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    preserveIconColors: Boolean = false
) {
    val colors = MaterialTheme.colorScheme

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (preserveIconColors) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = colors.onSurface
            )
        }

        Spacer(Modifier.size(12.dp))

        Text(
            text = text,
            color = colors.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}
