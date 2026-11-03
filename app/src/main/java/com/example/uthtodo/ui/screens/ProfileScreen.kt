package com.example.uthtodo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uthtodo.R
import com.google.firebase.auth.FirebaseUser
import java.time.LocalDate
import java.time.ZoneId
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalConfiguration
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ProfileScreen(
    user: FirebaseUser?,
    tasks: List<HomeTask>,
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val completed = tasks.count { it.done }
    val pending = tasks.size - completed
    val percent = if (tasks.isEmpty()) 0 else (completed * 100f / tasks.size).toInt()
    val anonymous = user == null || user.isAnonymous
    val displayName = if (anonymous) stringResource(R.string.uth_profile_guest)
    else user?.displayName?.takeIf { it.isNotBlank() }
        ?: user.email?.substringBefore('@')
        ?: stringResource(R.string.uth_profile_guest)
    val initials = if (anonymous) "K" else displayName.split(' ')
        .filter { it.isNotBlank() }.takeLast(2)
        .joinToString("") { it.take(1).uppercase() }.ifEmpty { "?" }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.nav_settings), color = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.uth_profile_title), fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(54.dp))
        }
        Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(Modifier.size(88.dp).background(Color(0xFFE6EDFF), CircleShape),
                contentAlignment = Alignment.Center) {
                Text(initials, color = MaterialTheme.colorScheme.primary, fontSize = 30.sp,
                    fontWeight = FontWeight.Bold)
            }
            Text(displayName, color = MaterialTheme.colorScheme.onBackground, fontSize = 23.sp,
                fontWeight = FontWeight.Bold)
            Text(
                if (anonymous) stringResource(R.string.uth_profile_unlinked)
                else user?.email ?: stringResource(R.string.uth_profile_connected),
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileStat(completed.toString(), stringResource(R.string.uth_profile_completed), Modifier.weight(1f))
                ProfileStat(pending.toString(), stringResource(R.string.uth_profile_pending), Modifier.weight(1f))
                ProfileStat("$percent%", stringResource(R.string.uth_profile_percentage), Modifier.weight(1f))
            }
            Column(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(stringResource(R.string.uth_profile_week), fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                WeeklyCompletionChart(tasks)

            }
        }
        TextButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Icon(painterResource(R.drawable.ic_profile_logout), contentDescription = null,
                tint = Color(0xFFC4321F), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.uth_profile_signout), color = Color(0xFFC4321F),
                fontSize = 16.sp)
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun ProfileStat(value: String, label: String, modifier: Modifier) {
    Column(modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
    }
}

/** Real completion events from Firestore completedAt; historical tasks may have no timestamp. */
@Composable
private fun WeeklyCompletionChart(tasks: List<HomeTask>) {
    val today = LocalDate.now()
    val start = today.minusDays(6)
    val counts = (0L..6L).map { offset ->
        val day = start.plusDays(offset)
        tasks.count { task ->
            task.completedAt?.toInstant()?.atZone(ZoneId.systemDefault())
                ?.toLocalDate() == day
        }
    }
    if (counts.all { it == 0 }) {
        Text(stringResource(R.string.uth_profile_chart_unavailable),
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        return
    }
    val maxCount = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    Row(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        counts.forEachIndexed { index, count ->
            val day = start.plusDays(index.toLong())
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Text(count.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp)
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .width(20.dp)
                        .height((8 + 83 * count / maxCount).dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                        )
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    day.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}