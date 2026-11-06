package com.example.uthtodo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.uthtodo.notifications.NotificationHistory
import com.example.uthtodo.notifications.TodoNotificationEntry
import com.example.uthtodo.notifications.TodoNotificationType
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class NotificationHistoryTest {
    @Test
    fun legacyRowsKeepContentAndDefaultToUnreadUntilRead() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val uid = "test-${UUID.randomUUID()}"
        val otherUid = "$uid-other"
        val preferences = context.getSharedPreferences("uth_notification_history", 0)
        val key = "notifications_$uid"
        val legacyTimestamp = System.currentTimeMillis() - 10_000L
        val legacyJson = JSONArray().put(
            JSONObject()
                .put("title", "Legacy title")
                .put("message", "Legacy body")
                .put("timestamp", legacyTimestamp)
        ).toString()

        try {
            preferences.edit().putString(key, legacyJson).commit()
            val migrated = NotificationHistory.read(context, uid).single()
            assertEquals("Legacy title", migrated.title)
            assertEquals("Legacy body", migrated.message)
            assertEquals(legacyTimestamp, migrated.timestamp)
            assertEquals("legacy-$legacyTimestamp-0", migrated.id)
            assertEquals(TodoNotificationType.OTHER, migrated.type)
            assertNull(migrated.readAt)
            assertFalse(migrated.isRead)

            NotificationHistory.markRead(context, uid, migrated.id)
            val read = NotificationHistory.read(context, uid).single()
            assertTrue(read.isRead)
            assertNotNull(read.readAt)
            assertTrue(NotificationHistory.read(context, otherUid).isEmpty())

            NotificationHistory.add(
                context, uid,
                TodoNotificationEntry("New", "Body", System.currentTimeMillis(), TodoNotificationType.REMINDER)
            )
            NotificationHistory.markAllRead(context, uid)
            assertTrue(NotificationHistory.read(context, uid).all { it.isRead })
        } finally {
            preferences.edit().remove(key).remove("notifications_$otherUid").commit()
        }
    }

    @Test
    fun notificationsOlderThanOneDayAreRemovedFromLocalHistory() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val uid = "expiry-${UUID.randomUUID()}"
        val preferences = context.getSharedPreferences("uth_notification_history", 0)
        val key = "notifications_$uid"
        val expiredTimestamp = System.currentTimeMillis() - 24L * 60L * 60L * 1000L - 1L
        try {
            preferences.edit().putString(
                key,
                JSONArray().put(
                    JSONObject().put("title", "Expired")
                        .put("message", "Old notification")
                        .put("timestamp", expiredTimestamp)
                ).toString()
            ).commit()

            assertTrue(NotificationHistory.read(context, uid).isEmpty())
            assertEquals("[]", preferences.getString(key, null))
        } finally {
            preferences.edit().remove(key).commit()
        }
    }
}
