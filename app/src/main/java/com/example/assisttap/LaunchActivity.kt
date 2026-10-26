package com.example.assisttap

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.Toast

/**
 * Invisible entry point: opens the default voice assistant and finishes immediately.
 *
 * Not a splash screen; lint's CustomSplashScreen check is a false positive here.
 */
@SuppressLint("CustomSplashScreen")
class LaunchActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val target =
            AssistantIntentResolver { it.resolveActivity(packageManager) != null }
                .resolve(IntentCandidates.build())
        if (target != null) {
            startActivity(target)
        } else {
            Toast.makeText(this, R.string.no_assistant_found, Toast.LENGTH_SHORT).show()
        }
        // Theme.NoDisplay requires finishing before onResume.
        finish()
    }
}
