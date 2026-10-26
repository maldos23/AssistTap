package com.example.assisttap

/**
 * Tells whether AssistTap opened the assistant recently enough that a new launch should not open it again.
 *
 * Used two ways by [LaunchActivity]: without [AssistantWatcherService] (which can see the assistant), a launch
 * inside [DEFAULT_WINDOW_MILLIS] means "close it"; with the watcher, a launch inside [OPENING_GRACE_MILLIS] means
 * "it is still appearing, don't start a second one".
 */
class AssistantToggle(
    private val windowMillis: Long = DEFAULT_WINDOW_MILLIS,
) {
    /**
     * Returns `true` when [openedAt] (a previous [now]-style timestamp, or [NEVER]) lies within the window before
     * [now]. A timestamp from the future (the clock reset on reboot) never counts.
     */
    fun openedRecently(
        now: Long,
        openedAt: Long,
    ): Boolean = openedAt != NEVER && now - openedAt in 0 until windowMillis

    /** Calibration knobs and sentinel values. */
    companion object {
        /**
         * Without the watcher: how long after opening a second launch still means "close". Kept well under the ~10 s an
         * unattended overlay stays up, so a press after the assistant closed by itself opens it instead of going home.
         */
        const val DEFAULT_WINDOW_MILLIS = 5_000L

        /** With the watcher: a double-tap debounce while the assistant appears; later presses are never swallowed. */
        const val OPENING_GRACE_MILLIS = 1_500L

        /** Marks "AssistTap has not opened the assistant" (or it was already closed). */
        const val NEVER = -1L
    }
}
