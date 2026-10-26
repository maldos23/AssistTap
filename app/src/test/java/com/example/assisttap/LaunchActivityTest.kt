package com.example.assisttap

import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowSystemClock
import org.robolectric.shadows.ShadowToast
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
class LaunchActivityTest {
    private fun registerVoiceCommandHandler() {
        val packageManager = ApplicationProvider.getApplicationContext<Application>().packageManager
        val handler =
            ResolveInfo().apply {
                activityInfo =
                    ActivityInfo().apply {
                        packageName = "com.example.voice"
                        name = "com.example.voice.VoiceActivity"
                    }
            }
        shadowOf(packageManager).addResolveInfoForIntent(Intent(Intent.ACTION_VOICE_COMMAND), handler)
    }

    /** Launches [LaunchActivity] once and returns the intent it started (or `null`), asserting it finished. */
    private fun launch(): Intent? {
        val activity = Robolectric.buildActivity(LaunchActivity::class.java).create().get()
        assertTrue(activity.isFinishing)
        return shadowOf(activity).nextStartedActivity
    }

    @Test
    fun `starts voice command intent and finishes`() {
        registerVoiceCommandHandler()

        assertEquals(Intent.ACTION_VOICE_COMMAND, launch()?.action)
    }

    @Test
    fun `shows toast and finishes when nothing resolves`() {
        assertNull(launch())
        assertEquals("No assistant found", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `second launch inside the window goes home to close the assistant`() {
        registerVoiceCommandHandler()
        launch()
        ShadowSystemClock.advanceBy(Duration.ofSeconds(3))

        val second = launch()

        assertEquals(Intent.ACTION_MAIN, second?.action)
        assertEquals(setOf(Intent.CATEGORY_HOME), second?.categories)
    }

    @Test
    fun `launch after closing opens the assistant again`() {
        registerVoiceCommandHandler()
        launch()
        launch()

        assertEquals(Intent.ACTION_VOICE_COMMAND, launch()?.action)
    }

    @Test
    fun `launch after the window opens the assistant again`() {
        registerVoiceCommandHandler()
        launch()
        ShadowSystemClock.advanceBy(Duration.ofMillis(AssistantToggle.DEFAULT_WINDOW_MILLIS))

        assertEquals(Intent.ACTION_VOICE_COMMAND, launch()?.action)
    }

    @Test
    fun `no assistant found does not arm the toggle`() {
        launch()
        registerVoiceCommandHandler()

        assertEquals(Intent.ACTION_VOICE_COMMAND, launch()?.action)
    }
}
