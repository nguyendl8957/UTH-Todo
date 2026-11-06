package com.example.uthtodo

import com.example.uthtodo.ui.components.avatarInitials
import org.junit.Assert.assertEquals
import org.junit.Test

class AvatarInitialsTest {
    @Test fun twoWordsUseBothInitials() = assertEquals("MA", avatarInitials("Minh Anh"))
    @Test fun multipleWordsUseLastTwo() = assertEquals("MA", avatarInitials("Nguyen Van Minh Anh"))
    @Test fun extraWhitespaceIsNormalized() = assertEquals("MA", avatarInitials("  Minh   Anh  "))
    @Test fun oneWordUsesOneInitial() = assertEquals("M", avatarInitials("Minh"))
    @Test fun blankNameUsesSafeFallback() = assertEquals("?", avatarInitials("   "))
    @Test fun guestUsesPrototypeInitial() = assertEquals("K", avatarInitials("Guest"))
}
