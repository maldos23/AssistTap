package com.example.assisttap

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RootAssistantTest {
    private val commands = mutableListOf<String>()
    private val prefs =
        ApplicationProvider.getApplicationContext<Application>().getSharedPreferences("test", Context.MODE_PRIVATE)
    private val root = RootAssistant(prefs, "root_1")

    private fun answer(vararg outputs: Pair<String, String?>) {
        RootAssistant.runner = { command, _ ->
            commands += command
            outputs.toMap()[command]
        }
    }

    @After
    fun tearDown() {
        RootAssistant.runner = { _, _ -> null }
    }

    @Test
    fun `unknown until checked`() {
        assertNull(root.available)
    }

    @Test
    fun `root confirmed only by uid 0`() {
        answer(
            "id" to "uid=0(root) gid=0(root)\n",
            "cmd voiceinteraction show" to "",
            "cmd voiceinteraction hide" to "",
        )

        assertTrue(root.check())
        assertEquals(true, root.available)
        assertTrue(root.show())
        assertTrue(root.hide())
        assertEquals(listOf("id", "cmd voiceinteraction show", "cmd voiceinteraction hide"), commands)
    }

    @Test
    fun `denied, missing or non-root su means not available and runs nothing else`() {
        for (output in listOf(null, "uid=2000(shell)\n")) {
            commands.clear()
            answer("id" to output)

            assertFalse(root.check())
            assertEquals(false, root.available)
            assertFalse(root.show())
            assertFalse(root.hide())
            assertEquals(listOf("id"), commands)
        }
    }

    @Test
    fun `a failing command is reported so the caller can fall back`() {
        answer("id" to "uid=0(root)", "cmd voiceinteraction show" to "Error: no active service\n")
        root.check()

        assertFalse(root.show())
        assertFalse(root.hide())
    }

    @Test
    fun `a new install key checks again`() {
        answer("id" to "uid=0(root)")
        root.check()

        assertNull(RootAssistant(prefs, "root_2").available)
    }
}
