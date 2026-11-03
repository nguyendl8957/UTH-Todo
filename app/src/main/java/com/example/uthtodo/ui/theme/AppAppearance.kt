package com.example.uthtodo.ui.theme

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/** Appearance is intentionally kept separate from Firebase account data. */
class AppAppearance(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        "uth_todo_settings", Context.MODE_PRIVATE
    )

    var themeMode by mutableStateOf(prefs.getString("theme_mode", "light") ?: "light")
        private set
    var accentIndex by mutableIntStateOf(prefs.getInt("accent_index", 0).coerceIn(0, 3))
        private set
    var languageTag by mutableStateOf(prefs.getString("language", "vi") ?: "vi")
        private set

    val accentColor: Color
        get() = listOf(
            Color(0xFF2F5BEA), Color(0xFF0F8A5F),
            Color(0xFFC2410C), Color(0xFF7C3AED)
        )[accentIndex]

    fun isDark(systemDark: Boolean): Boolean = when (themeMode) {
        "dark" -> true
        "system" -> systemDark
        else -> false
    }

    fun setTheme(mode: String) {
        if (mode !in listOf("light", "dark", "system")) return
        themeMode = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    fun setAccent(index: Int) {
        accentIndex = index.coerceIn(0, 3)
        prefs.edit().putInt("accent_index", accentIndex).apply()
    }

    fun setLanguage(tag: String) {
        if (tag !in listOf("vi", "en", "zh-CN", "ja", "ko")) return
        languageTag = tag
        prefs.edit().putString("language", tag).apply()
    }
}

/** Recreates resource context after the language changes, including on Android < 13. */
@Composable
fun LocalizedContent(languageTag: String, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val locale = Locale.forLanguageTag(languageTag)
    val config = Configuration(context.resources.configuration).apply {
        setLocale(locale)
        setLayoutDirection(locale)
    }
    val localizedContext = context.createConfigurationContext(config)
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides config,
        content = content
    )
}