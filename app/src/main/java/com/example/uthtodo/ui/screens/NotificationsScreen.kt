package com.example.uthtodo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uthtodo.R
import com.example.uthtodo.ui.components.TodoBottomBar
import com.example.uthtodo.ui.components.TodoTab

/** Không tạo lịch sử thông báo giả: chưa tích hợp Android notification history. */
@Composable
fun NotificationsScreen(
    onHomeClick: () -> Unit,
    onTasksClick: () -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Color(0xFFF5F5F7)).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.nav_notifications), fontSize = 28.sp,
                fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1E))
            // Chưa có bản ghi, không hiển thị nút "đánh dấu tất cả" giả.
        }
        Box(
            Modifier.weight(1f).fillMaxWidth().padding(20.dp)
                .background(Color.White, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                stringResource(R.string.uth_notifications_empty),
                modifier = Modifier.padding(20.dp), fontSize = 14.sp,
                color = Color(0xFF6B6B70)
            )
        }
        TodoBottomBar(
            selectedTab = TodoTab.NOTIFICATIONS,
            onHomeClick = onHomeClick,
            onTasksClick = onTasksClick,
            onAddClick = onAddClick,
            onNotificationsClick = {},
            onSettingsClick = onSettingsClick
        )
    }
}
