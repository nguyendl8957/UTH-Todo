package com.example.uthtodo.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.uthtodo.R
import com.example.uthtodo.ui.theme.UTHColors
import androidx.compose.animation.core.LinearEasing

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    // Sau 2 giây, báo cho màn hình cha chuyển tiếp.
    LaunchedEffect(Unit) {
        delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UTHColors.Primary)
            .clickable { onFinished() }
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .clip(RoundedCornerShape(27.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        id = R.drawable.ic_uth_todo_logo
                    ),
                    contentDescription = "Logo UTH Todo",
                    modifier = Modifier.size(88.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "UTH Todo",
                    fontSize = 40.sp,
                    lineHeight = 46.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "Việc học, việc nhà — gọn gàng mỗi ngày",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-56).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SplashLoadingBar()

            Text(
                text = "Phiên bản 1.0",
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}



@Composable
private fun SplashLoadingBar() {
    val transition = rememberInfiniteTransition(
        label = "SplashLoading"
    )

    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "LoadingProgress"
    )

    val head = 120f * progress

    val tail = if (progress <= 0.65f) {
        (head - 48f).coerceAtLeast(0f)
    } else {
        val shrinking = (progress - 0.65f) / 0.35f
        val startTail = (120f * 0.65f - 48f)
        startTail + (120f - startTail) * shrinking
    }

    val barWidth = (head - tail).coerceAtLeast(0f)

    Box(
        modifier = Modifier
            .width(120.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color.White.copy(alpha = 0.28f))
    ) {
        Box(
            modifier = Modifier
                .offset(x = tail.dp)
                .width(barWidth.dp)
                .height(4.dp)
                .background(Color.White)
        )
    }
}




