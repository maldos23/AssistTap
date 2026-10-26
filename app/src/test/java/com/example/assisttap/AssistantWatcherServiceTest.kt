package com.example.assisttap

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowSystemClock
import org.robolectric.util.ReflectionHelpers
import org.robolectric.util.ReflectionHelpers.ClassParameter
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
class AssistantWatcherServiceTest {
    private lateinit var service: AssistantWatcherService

    @Before
    fun setUp() {
        val handler =
            ResolveInfo().apply {
                activityInfo =
                    ActivityInfo().apply {
                        packageName = ASSISTANT
                        name = "$ASSISTANT.VoiceActivity"
                    }
            }
        val packageManager = ApplicationProvider.getApplicationContext<android.app.Application>().packageManager
        shadowOf(packageManager).addResolveInfoForIntent(Intent(Intent.ACTION_VOICE_COMMAND), handler)
        service = Robolectric.setupService(AssistantWatcherService::class.java)
        service.onServiceConnected()
    }

    @After
    fun tearDown() {
        service.onUnbind(null)
    }

    /** Simulates [packageName] opening window [id]: the state-change event, then the window being on screen. */
    private fun showWindowOf(
        packageName: String,
        id: Int = 7,
    ) {
        val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
        event.packageName = packageName
        ReflectionHelpers.callInstanceMethod<Unit>(
            event,
            "setWindowId",
            ClassParameter.from(Int::class.javaPrimitiveType, id),
        )
        service.onAccessibilityEvent(event)
        val window = AccessibilityWindowInfo.obtain()
        shadowOf(window).setId(id)
        shadowOf(service).setWindows(listOf(window))
    }

    private fun launch(): Intent? {
        val activity = Robolectric.buildActivity(LaunchActivity::class.java).create().get()
        assertTrue(activity.isFinishing)
        return shadowOf(activity).nextStartedActivity
    }

    @Test
    fun `isShowing matches on-screen windows by package`() {
        showWindowOf(ASSISTANT)

        assertTrue(service.isShowing(ASSISTANT))
        assertFalse(service.isShowing("com.other"))
    }

    @Test
    fun `forgets windows that are gone`() {
        showWindowOf(ASSISTANT)
        shadowOf(service).setWindows(emptyList())
        assertFalse(service.isShowing(ASSISTANT))

        // Same id reused by a window it never saw an event for: not the assistant.
        val reused = AccessibilityWindowInfo.obtain()
        shadowOf(reused).setId(7)
        shadowOf(service).setWindows(listOf(reused))
        assertFalse(service.isShowing(ASSISTANT))
    }

    @Test
    fun `ignores events that are not window state changes`() {
        service.onAccessibilityEvent(null)
        service.onAccessibilityEvent(
            AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_CLICKED).apply {
                packageName =
                    ASSISTANT
            },
        )
        assertFalse(service.isShowing(ASSISTANT))
    }

    @Test
    fun `instance is cleared on unbind`() {
        assertEquals(service, AssistantWatcherService.instance)
        service.onUnbind(null)
        assertNull(AssistantWatcherService.instance)
    }

    @Test
    fun `closes a visible assistant with back and starts nothing`() {
        showWindowOf(ASSISTANT)

        assertNull(launch())
        assertEquals(listOf(AccessibilityService.GLOBAL_ACTION_BACK), shadowOf(service).globalActionsPerformed)
    }

    @Test
    fun `ignores a launch while the assistant is still appearing`() {
        launch()
        ShadowSystemClock.advanceBy(Duration.ofSeconds(1))

        assertNull(launch())
        assertTrue(shadowOf(service).globalActionsPerformed.isEmpty())
    }

    @Test
    fun `opens again after the grace period when the assistant is gone`() {
        launch()
        ShadowSystemClock.advanceBy(Duration.ofMillis(AssistantToggle.OPENING_GRACE_MILLIS))

        // Still inside the time-based window, but the watcher knows better: no home, open again.
        assertEquals(Intent.ACTION_VOICE_COMMAND, launch()?.action)
    }

    @Test
    fun `opens again right after closing it`() {
        launch()
        showWindowOf(ASSISTANT)
        launch()
        shadowOf(service).setWindows(emptyList())

        assertEquals(Intent.ACTION_VOICE_COMMAND, launch()?.action)
    }

    private companion object {
        const val ASSISTANT = "com.example.voice"
    }
}
