
package com.example.uthtodo.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.uthtodo.R
import com.example.uthtodo.data.OnboardingContent
import com.example.uthtodo.ui.theme.UTHColors
import androidx.compose.ui.res.stringResource

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val pages = OnboardingContent.pages

    var pageIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    val page = pages[pageIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding()
            .padding(
                start = 28.dp,
                end = 28.dp,
                top = 16.dp,
                bottom = 24.dp
            )
    ) {

        // 1. Logo ứng dụng và nút Bỏ qua
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(UTHColors.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            R.drawable.ic_onboarding_1_1
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.ExtraBold,
                                color = UTHColors.TextPrimary
                            )
                        ) {
                            append("UTH")
                        }

                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Medium,
                                color = UTHColors.Primary
                            )
                        ) {
                            append(" Todo")
                        }
                    },
                    fontSize = 19.sp
                )
            }

            TextButton(onClick = onFinished) {
                Text(
                    text = stringResource(R.string.onboarding_skip),
                    color = UTHColors.TextSecondary,
                    fontSize = 15.sp
                )
            }
        }

        // 2. Hình minh họa nằm ở vùng giữa
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(
                        UTHColors.Primary.copy(alpha = 0.10f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(RoundedCornerShape(56.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            page.illustrationRes
                        ),
                        contentDescription = stringResource(page.titleRes),
                        modifier = Modifier.size(52.dp)
                    )
                }
            }
        }

        // 3. Tiêu đề và mô tả
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(page.titleRes),
                fontSize = 28.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                color = UTHColors.TextPrimary
            )

            Text(
                text = stringResource(page.descriptionRes),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = UTHColors.TextSecondary
            )
        }

        // 4. Ba chấm tiến độ có thể bấm
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            pages.forEachIndexed { index, _ ->
                val dotWidth by animateDpAsState(
                    targetValue = if (index == pageIndex) {
                        24.dp
                    } else {
                        8.dp
                    },
                    label = "OnboardingDot"
                )

                Box(
                    modifier = Modifier
                        .width(dotWidth)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (index == pageIndex) {
                                UTHColors.Primary
                            } else {
                                Color(0xFFC7C7CC)
                            }
                        )
                        .clickable {
                            pageIndex = index
                        }
                )
            }
        }

        // 5. Nút chính
        Button(
            onClick = {
                if (pageIndex < pages.lastIndex) {
                    pageIndex++
                } else {
                    onFinished()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = UTHColors.Primary,
                contentColor = Color.White
            )
        ) {
            Text(
                text = stringResource(page.buttonTextRes),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
