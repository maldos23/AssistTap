package com.example.assisttap

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast

/**
 * Invisible entry point: opens the default voice assistant and finishes immediately. Launched while the assistant
 * is open, it closes it instead: exactly via [AssistantWatcherService] when enabled, otherwise by time
 * ([AssistantToggle]).
 *
 * Not a splash screen; lint's CustomSplashScreen check is a false positive here.
 */
@SuppressLint("CustomSplashScreen")
class LaunchActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val now = SystemClock.elapsedRealtime()
        val openedAt = prefs.getLong(KEY_OPENED_AT, AssistantToggle.NEVER)
        val target =
            AssistantIntentResolver { it.resolveActivity(packageManager) != null }
                .resolve(IntentCandidates.build())
        val assistantPackage = target?.resolveActivity(packageManager)?.packageName
        val watcher = AssistantWatcherService.instance

        when {
            watcher != null && assistantPackage != null && watcher.isShowing(assistantPackage) -> {
                prefs.edit().remove(KEY_OPENED_AT).apply()
                watcher.close()
            }
            // Opened moments ago but not on screen yet: starting it again would duplicate the request.
            watcher != null && AssistantToggle(AssistantToggle.OPENING_GRACE_MILLIS).openedRecently(now, openedAt) ->
                Unit
            watcher == null && AssistantToggle().openedRecently(now, openedAt) -> {
                prefs.edit().remove(KEY_OPENED_AT).apply()
                // Going home is the one action a normal app can take that dismisses the assistant overlay.
                startActivity(IntentCandidates.home())
            }
            target != null -> {
                startActivity(target)
                prefs.edit().putLong(KEY_OPENED_AT, now).apply()
            }
            else -> Toast.makeText(this, R.string.no_assistant_found, Toast.LENGTH_SHORT).show()
        }
        // Theme.NoDisplay requires finishing before onResume.
        finish()
    }

    private companion object {
        const val PREFS_NAME = "assisttap"
        const val KEY_OPENED_AT = "assistant_opened_at"
    }
}
