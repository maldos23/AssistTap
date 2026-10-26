package com.example.assisttap

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
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
class LaunchActivityTest {
    @Test
    fun `starts voice command intent and finishes`() {
        val packageManager = ApplicationProvider.getApplicationContext<android.app.Application>().packageManager
        val handler =
            ResolveInfo().apply {
                activityInfo =
                    ActivityInfo().apply {
                        packageName = "com.example.voice"
                        name = "com.example.voice.VoiceActivity"
                    }
            }
        shadowOf(packageManager).addResolveInfoForIntent(Intent(Intent.ACTION_VOICE_COMMAND), handler)

        val activity = Robolectric.buildActivity(LaunchActivity::class.java).create().get()

        assertEquals(Intent.ACTION_VOICE_COMMAND, shadowOf(activity).nextStartedActivity.action)
        assertTrue(activity.isFinishing)
    }

    @Test
    fun `shows toast and finishes when nothing resolves`() {
        val activity = Robolectric.buildActivity(LaunchActivity::class.java).create().get()

        assertNull(shadowOf(activity).nextStartedActivity)
        assertEquals("No assistant found", ShadowToast.getTextOfLatestToast())
        assertTrue(activity.isFinishing)
    }
}
