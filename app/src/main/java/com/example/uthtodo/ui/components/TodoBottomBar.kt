package com.example.uthtodo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uthtodo.R

/** Dùng đúng hình học thanh dưới đã chạy tại Home và Tasks:
 * 70dp, padding ngang 8dp, nút + 56dp, offset -16dp, 5 vị trí bằng nhau.
 */
enum class TodoTab { HOME, TASKS, NOTIFICATIONS, SETTINGS }

@Composable
fun TodoBottomBar(
    selectedTab: TodoTab,
    onHomeClick: () -> Unit,
    onTasksClick: () -> Unit,
    onAddClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    accent: Color = Color.Unspecified
) {
    val activeAccent = if (accent == Color.Unspecified) MaterialTheme.colorScheme.primary else accent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .height(70.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TodoNavItem(R.drawable.ic_nav_home, stringResource(R.string.nav_home),
            selectedTab == TodoTab.HOME, activeAccent, onHomeClick, Modifier.weight(1f))
        TodoNavItem(R.drawable.ic_nav_tasks, stringResource(R.string.nav_tasks),
            selectedTab == TodoTab.TASKS, activeAccent, onTasksClick, Modifier.weight(1f))
        Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .offset(y = (-16).dp)
                    .size(56.dp)
                    .shadow(elevation = 12.dp, shape = CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(activeAccent)
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_nav_add),
                    contentDescription = stringResource(R.string.nav_add),
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        TodoNavItem(R.drawable.ic_nav_bell, stringResource(R.string.nav_notifications),
            selectedTab == TodoTab.NOTIFICATIONS, activeAccent, onNotificationsClick, Modifier.weight(1f))
        TodoNavItem(R.drawable.ic_nav_settings, stringResource(R.string.nav_settings),
            selectedTab == TodoTab.SETTINGS, activeAccent, onSettingsClick, Modifier.weight(1f))
    }
}

@Composable
private fun TodoNavItem(
    icon: Int,
    title: String,
    active: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (active) accent else Color(0xFF6B6B70)
    Column(
        modifier = modifier.clickable(onClick = onClick).padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(painterResource(icon), null, modifier = Modifier.size(24.dp), tint = color)
        Text(title, fontSize = 11.sp, color = color, maxLines = 1,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium)
    }
}
