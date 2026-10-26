package com.example.assisttap

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LaunchSmokeTest {
    @Test
    fun launchesWithoutCrash() {
        // The activity finishes itself in onCreate; reaching here without an exception is the pass condition.
        ActivityScenario.launch(LaunchActivity::class.java).close()
    }
}
