package com.example.uthtodo.data.task

import org.junit.Assert.assertEquals
import org.junit.Test

class TaskSubtaskMapperTest {
    @Test
    fun mapsLegacyRowsWithStableIdsAndPreservesOrderAndCompletion() {
        val rows = listOf(
            mapOf("title" to "First", "done" to true),
            mapOf("title" to "Second", "done" to false)
        )

        val firstRead = mapTaskSubtasks("task-1", rows)
        val secondRead = mapTaskSubtasks("task-1", rows)

        assertEquals(listOf("First", "Second"), firstRead.map { it.title })
        assertEquals(listOf(true, false), firstRead.map { it.done })
        assertEquals(listOf("legacy-task-1-0", "legacy-task-1-1"), firstRead.map { it.id })
        assertEquals(firstRead, secondRead)
    }

    @Test
    fun keepsExistingIdsAndDefaultsInvalidDoneToFalse() {
        val rows = listOf(mapOf("id" to "stored-id", "title" to "Existing", "done" to "bad"))

        val result = mapTaskSubtasks("task-2", rows)

        assertEquals("stored-id", result.single().id)
        assertEquals("Existing", result.single().title)
        assertEquals(false, result.single().done)
    }

    @Test
    fun emptyAndMalformedRowsMapSafely() {
        assertEquals(emptyList<Any>(), mapTaskSubtasks("task-3", emptyList<Any>()))
        assertEquals(emptyList<Any>(), mapTaskSubtasks("task-3", null))
        assertEquals(
            listOf("Valid"),
            mapTaskSubtasks(
                "task-3",
                listOf("not-a-map", mapOf("title" to "Valid", "done" to true), mapOf("done" to false))
            ).map { it.title }
        )
    }
}
