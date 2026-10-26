package com.example.assisttap

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

/**
 * Optional accessibility service that lets AssistTap see whether the assistant is on screen and close it with Back.
 *
 * Normal apps cannot see other apps' windows; once the user enables this service in Accessibility settings,
 * [LaunchActivity] uses it instead of the time-based [AssistantToggle]. It listens to no events and reads no content
 * beyond the package name of each on-screen window.
 */
class AssistantWatcherService : AccessibilityService() {
    public override fun onServiceConnected() {
        instance = this
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    /** Returns `true` when any on-screen window belongs to [packageName]. */
    fun isShowing(packageName: String): Boolean = windows.any { it.root?.packageName == packageName }

    /** Closes the assistant the way the user would: a Back press, which returns to the previous app. */
    fun close() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    /** Access to the connected service. */
    companion object {
        /** The running service while the user has it enabled, else `null`. Cleared in onUnbind, so it never leaks. */
        @SuppressLint("StaticFieldLeak")
        @Volatile
        var instance: AssistantWatcherService? = null
            private set
    }
}
