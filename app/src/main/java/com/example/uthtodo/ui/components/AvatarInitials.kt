package com.example.uthtodo.ui.components

import java.util.Locale

/** Shared initials rule: last two name parts, with the prototype's guest marker. */
fun avatarInitials(displayName: String?, isGuest: Boolean = false): String {
    if (isGuest || displayName?.trim()?.equals("Guest", ignoreCase = true) == true) {
        return "K"
    }

    val parts = displayName.orEmpty().trim().split(Regex("\\s+")).filter(String::isNotBlank)
    return parts.takeLast(2)
        .mapNotNull { it.firstOrNull()?.toString()?.uppercase(Locale.ROOT) }
        .joinToString("")
        .ifBlank { "?" }
}
