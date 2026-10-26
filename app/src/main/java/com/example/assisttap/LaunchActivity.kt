package com.example.assisttap

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Invisible entry point: opens the default voice assistant and finishes immediately. Launched while the assistant
 * is open, it closes it instead: exactly via [AssistantWatcherService] when enabled, otherwise by time
 * ([AssistantToggle]). With root ([RootAssistant]) it opens and closes through the system's assistant path.
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
        val root = RootAssistant(prefs, KEY_ROOT_PREFIX + packageManager.getPackageInfo(packageName, 0).lastUpdateTime)
        if (root.available == null) checkRootInBackground(root, applicationContext)

        when {
            watcher != null && assistantPackage != null && watcher.isShowing(assistantPackage) -> {
                prefs.edit().remove(KEY_OPENED_AT).apply()
                if (!root.hide()) watcher.close()
            }
            // Opened moments ago but not on screen yet: starting it again would duplicate the request.
            watcher != null && AssistantToggle(AssistantToggle.OPENING_GRACE_MILLIS).openedRecently(now, openedAt) ->
                Unit
            watcher == null && AssistantToggle().openedRecently(now, openedAt) -> {
                prefs.edit().remove(KEY_OPENED_AT).apply()
                // Going home is the one action a normal app can take that dismisses the assistant overlay.
                if (!root.hide()) startActivity(IntentCandidates.home())
            }
            root.show() -> prefs.edit().putLong(KEY_OPENED_AT, now).apply()
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
        const val KEY_ROOT_PREFIX = "root_available_"
        const val TAG = "AssistTap"
        val checkingRoot = AtomicBoolean()

        /** Runs the one-time root check without delaying this press, then tells the user which mode they got. */
        fun checkRootInBackground(
            root: RootAssistant,
            context: Context,
        ) {
            // A second press during the check must not open a second root prompt.
            if (!checkingRoot.compareAndSet(false, true)) return
            Thread {
                val available = root.check()
                checkingRoot.set(false)
                Log.i(TAG, "Root available: $available")
                Handler(Looper.getMainLooper()).post {
                    val message = if (available) R.string.root_available else R.string.root_unavailable
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }.start()
        }
    }
}
