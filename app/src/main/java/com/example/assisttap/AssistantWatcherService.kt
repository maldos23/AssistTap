package com.example.assisttap

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

/**
 * Optional accessibility service that lets AssistTap see whether the assistant is on screen and close it with Back.
 *
 * Normal apps cannot see other apps' windows; once the user enables this service in Accessibility settings,
 * [LaunchActivity] uses it instead of the time-based [AssistantToggle]. It reads nothing beyond the package name each
 * window-state event already carries. It never reads window content: that is a synchronous call into every on-screen
 * app, which blocked the launch for seconds when one of them was busy.
 */
class AssistantWatcherService : AccessibilityService() {
    public override fun onServiceConnected() {
        instance = this
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    /** Owner package of each window seen appearing. Touched only on the main thread. */
    private val owners = HashMap<Int, String>()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName ?: return
        if (event.eventType ==
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        ) {
            owners[event.windowId] = packageName.toString()
        }
    }

    override fun onInterrupt() = Unit

    /** Returns `true` when any on-screen window belongs to [packageName]. */
    fun isShowing(packageName: String): Boolean {
        val live = windows.map { it.id }
        owners.keys.retainAll(live.toSet())
        return live.any { owners[it] == packageName }
    }

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
