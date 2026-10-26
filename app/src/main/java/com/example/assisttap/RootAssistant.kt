package com.example.assisttap

import android.content.SharedPreferences
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Opens and closes the assistant through the system's own voice-interaction path (`cmd voiceinteraction show|hide`,
 * the same path as a hardware assist button) when the device grants AssistTap root.
 *
 * Measured on an Android 15 emulator: `show` is visible 0.5 s (warm) to 0.9 s (cold) sooner than the
 * `VOICE_COMMAND` intent, and `hide` closes both assistant variants, which HOME cannot. Every call reports failure
 * instead of throwing, so [LaunchActivity] falls back to the 1.1 behaviour.
 *
 * @param prefs where the result of [check] is kept; [stateKey] changes on reinstall/update, so a new build re-checks.
 */
class RootAssistant(
    private val prefs: SharedPreferences,
    private val stateKey: String,
) {
    /** `true`/`false` once [check] has run for this install, `null` before. */
    val available: Boolean?
        get() = if (prefs.contains(stateKey)) prefs.getBoolean(stateKey, false) else null

    /**
     * Asks for root and stores the answer. Blocks for up to [PROBE_TIMEOUT_MILLIS] (the root manager may show a grant
     * prompt), so call it off the main thread. Only a real `uid=0` counts: a missing `su`, a denial, or a `su` that
     * apps may not run (the emulator's) all mean "not available".
     */
    fun check(): Boolean =
        (runner("id", PROBE_TIMEOUT_MILLIS)?.contains("uid=0") == true)
            .also { prefs.edit().putBoolean(stateKey, it).apply() }

    /** Shows the assistant; `false` when root is not confirmed or the command failed. */
    fun show(): Boolean = voiceInteraction("show")

    /** Hides the assistant; `false` when root is not confirmed or the command failed. */
    fun hide(): Boolean = voiceInteraction("hide")

    // `cmd` prints nothing on success and an error (e.g. no active assistant, Android < 11) otherwise.
    private fun voiceInteraction(action: String): Boolean =
        available == true && runner("cmd voiceinteraction $action", COMMAND_TIMEOUT_MILLIS)?.isBlank() == true

    /** Timeouts and the command runner. */
    companion object {
        /** Long enough for the user to answer the root manager's grant prompt on first use. */
        const val PROBE_TIMEOUT_MILLIS = 15_000L

        /** Upper bound for one `su` call per press; a granted call takes a few hundred ms. */
        const val COMMAND_TIMEOUT_MILLIS = 1_500L

        /** Runs a command as root and returns its output, or `null` on failure. Replaced in tests. */
        @Volatile
        var runner: (command: String, timeoutMillis: Long) -> String? = ::su
    }
}

private fun su(
    command: String,
    timeoutMillis: Long,
): String? =
    try {
        val process = ProcessBuilder("su", "-c", command).redirectErrorStream(true).start()
        if (process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)) {
            process.inputStream
                .bufferedReader()
                .readText()
                .takeIf { process.exitValue() == 0 }
        } else {
            process.destroy()
            null
        }
    } catch (_: IOException) {
        null // No su binary.
    } catch (_: InterruptedException) {
        null
    }
