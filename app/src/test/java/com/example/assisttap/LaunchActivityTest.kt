package com.example.assisttap

import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowSystemClock
import org.robolectric.shadows.ShadowToast
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
class LaunchActivityTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val rootCommands = mutableListOf<String>()

    /** Fake `su`: never touches the host. `id` answers per [rootId]; voice-interaction commands per [rootWorks]. */
    private var rootId: String? = null
    private var rootWorks = true

    @Before
    fun fakeRoot() {
        RootAssistant.runner = { command, _ ->
            synchronized(rootCommands) { rootCommands += command }
            if (command == "id") rootId else "".takeIf { rootWorks }
        }
    }

    /** Stores the root check result as if it had already run for this install. */
    private fun rootChecked(available: Boolean) {
        val installed = app.packageManager.getPackageInfo(app.packageName, 0).lastUpdateTime
        app
            .getSharedPreferences("assisttap", 0)
            .edit()
            .putBoolean("root_available_$installed", available)
            .commit()
    }

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

    @Test
    fun `with root opens through the system assistant path`() {
        registerVoiceCommandHandler()
        rootChecked(true)

        assertNull(launch())
        assertEquals(listOf("cmd voiceinteraction show"), rootCommands)
    }

    @Test
    fun `with root closes with hide instead of going home`() {
        registerVoiceCommandHandler()
        rootChecked(true)
        launch()

        assertNull(launch())
        assertEquals(listOf("cmd voiceinteraction show", "cmd voiceinteraction hide"), rootCommands)
    }

    @Test
    fun `falls back to the intent and home when a root command fails`() {
        registerVoiceCommandHandler()
        rootChecked(true)
        rootWorks = false

        assertEquals(Intent.ACTION_VOICE_COMMAND, launch()?.action)
        assertEquals(setOf(Intent.CATEGORY_HOME), launch()?.categories)
    }

    @Test
    fun `checks root once in the background and tells the user`() {
        registerVoiceCommandHandler()
        rootId = "uid=0(root)"

        // The first press must not wait for the check.
        assertEquals(Intent.ACTION_VOICE_COMMAND, launch()?.action)
        val deadline = System.currentTimeMillis() + 5_000
        while (synchronized(rootCommands) { "id" !in rootCommands } || ShadowLooper.shadowMainLooper().isIdle) {
            if (System.currentTimeMillis() > deadline) break
            Thread.sleep(10)
        }
        ShadowLooper.idleMainLooper()

        assertEquals("AssistTap: root available, fast mode", ShadowToast.getTextOfLatestToast())
        assertEquals(listOf("id"), rootCommands)
    }
}
