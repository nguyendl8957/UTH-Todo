package com.example.uthtodo.data.task

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class RelativeTaskDate { YESTERDAY, TODAY, TOMORROW, ABSOLUTE, INVALID }

/** Task dates are calendar dates, not instants; parsing never applies a timezone shift. */
fun classifyTaskDate(date: String, today: LocalDate): RelativeTaskDate {
    val parsed = runCatching { LocalDate.parse(date) }.getOrNull()
        ?: return RelativeTaskDate.INVALID
    return when (parsed) {
        today.minusDays(1) -> RelativeTaskDate.YESTERDAY
        today -> RelativeTaskDate.TODAY
        today.plusDays(1) -> RelativeTaskDate.TOMORROW
        else -> RelativeTaskDate.ABSOLUTE
    }
}

/** Compares a local calendar deadline with the supplied local date and time. */
fun isTaskOverdue(date: String, time: String, now: LocalDateTime, done: Boolean): Boolean {
    if (done) return false
    val deadline = runCatching { LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(time)) }
        .getOrNull() ?: return false
    return deadline.isBefore(now)
}
