package com.example.assisttap

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AssistantToggleTest {
    private val toggle = AssistantToggle(windowMillis = 10_000)

    @Test
    fun `counts launches inside the window`() {
        assertTrue(toggle.openedRecently(now = 5_000, openedAt = 5_000))
        assertTrue(toggle.openedRecently(now = 14_999, openedAt = 5_000))
    }

    @Test
    fun `does not count once the window has passed`() {
        assertFalse(toggle.openedRecently(now = 15_000, openedAt = 5_000))
    }

    @Test
    fun `does not count when never opened or already closed`() {
        assertFalse(toggle.openedRecently(now = 5_000, openedAt = AssistantToggle.NEVER))
    }

    @Test
    fun `does not count a future time after a reboot`() {
        assertFalse(toggle.openedRecently(now = 1_000, openedAt = 5_000))
    }
}
