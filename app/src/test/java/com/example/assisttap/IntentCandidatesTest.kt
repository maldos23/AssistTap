package com.example.assisttap

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// Robolectric: android.content.Intent is a stub on the plain JVM.
@RunWith(RobolectricTestRunner::class)
class IntentCandidatesTest {
    @Test
    fun `candidate order is voice command then assist then google app`() {
        val candidates = IntentCandidates.build()

        assertEquals(
            listOf(Intent.ACTION_VOICE_COMMAND, Intent.ACTION_ASSIST, Intent.ACTION_MAIN),
            candidates.map { it.action },
        )
        assertEquals(listOf(null, null, IntentCandidates.GOOGLE_APP_PACKAGE), candidates.map { it.`package` })
        assertEquals(setOf(Intent.CATEGORY_LAUNCHER), candidates[2].categories)
    }

    @Test
    fun `home intent targets the home screen in a new task`() {
        val home = IntentCandidates.home()

        assertEquals(Intent.ACTION_MAIN, home.action)
        assertEquals(setOf(Intent.CATEGORY_HOME), home.categories)
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, home.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
