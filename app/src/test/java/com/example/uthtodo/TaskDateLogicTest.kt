package com.example.uthtodo

import com.example.uthtodo.data.task.RelativeTaskDate
import com.example.uthtodo.data.task.classifyTaskDate
import com.example.uthtodo.data.task.isTaskOverdue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class TaskDateLogicTest {
    private val nov8 = LocalDate.parse("2026-11-08")

    @Test fun relativeDatesAdvanceAndThenBecomeAbsolute() {
        assertEquals(RelativeTaskDate.YESTERDAY, classifyTaskDate("2026-11-07", nov8))
        assertEquals(RelativeTaskDate.TODAY, classifyTaskDate("2026-11-08", nov8))
        assertEquals(RelativeTaskDate.TOMORROW, classifyTaskDate("2026-11-09", nov8))
        assertEquals(RelativeTaskDate.ABSOLUTE, classifyTaskDate("2026-11-06", nov8))
        assertEquals(RelativeTaskDate.ABSOLUTE,
            classifyTaskDate("2026-11-07", LocalDate.parse("2026-11-09")))
    }

    @Test fun midnightAndYearBoundaryUseCalendarDates() {
        assertEquals(RelativeTaskDate.TODAY,
            classifyTaskDate("2026-11-08", LocalDateTime.parse("2026-11-08T00:00").toLocalDate()))
        assertEquals(RelativeTaskDate.YESTERDAY,
            classifyTaskDate("2026-11-07", LocalDateTime.parse("2026-11-08T00:00").toLocalDate()))
        assertEquals(RelativeTaskDate.YESTERDAY,
            classifyTaskDate("2026-12-31", LocalDate.parse("2027-01-01")))
        assertEquals(RelativeTaskDate.TOMORROW,
            classifyTaskDate("2027-01-01", LocalDate.parse("2026-12-31")))
    }

    @Test fun calendarDateDoesNotShiftWhenTimezoneChanges() {
        val storedDate = "2026-11-08"
        val originalZone = java.util.TimeZone.getDefault()
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Los_Angeles"))
            assertEquals(RelativeTaskDate.TODAY, classifyTaskDate(storedDate, nov8))
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Tokyo"))
            assertEquals(RelativeTaskDate.TODAY, classifyTaskDate(storedDate, nov8))
        } finally {
            java.util.TimeZone.setDefault(originalZone)
        }
    }

    @Test fun overdueRequiresPastTimeAndPendingTask() {
        assertFalse(isTaskOverdue("2026-11-08", "19:00", LocalDateTime.parse("2026-11-08T18:59"), false))
        assertTrue(isTaskOverdue("2026-11-08", "19:00", LocalDateTime.parse("2026-11-08T20:00"), false))
        assertTrue(isTaskOverdue("2026-11-07", "19:00", LocalDateTime.parse("2026-11-08T00:00"), false))
        assertFalse(isTaskOverdue("2026-11-08", "19:00", LocalDateTime.parse("2026-11-08T20:00"), true))
        assertFalse(isTaskOverdue("bad-date", "19:00", LocalDateTime.parse("2026-11-08T20:00"), false))
    }

    @Test fun invalidStoredDateIsClassifiedWithoutCrashing() {
        assertEquals(RelativeTaskDate.INVALID, classifyTaskDate("2026-02-30", nov8))
    }
}
