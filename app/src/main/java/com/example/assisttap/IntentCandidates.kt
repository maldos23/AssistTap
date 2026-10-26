package com.example.assisttap

import android.content.Intent

/**
 * Builds the ordered list of intents that may open a voice assistant.
 *
 * Order matters: the first resolvable intent wins (see [AssistantIntentResolver]).
 */
object IntentCandidates {
    /** Package of the Google app, which hosts Google Assistant / Gemini on most devices. */
    const val GOOGLE_APP_PACKAGE = "com.google.android.googlequicksearchbox"

    /** Returns `VOICE_COMMAND`, then `ASSIST`, then the Google app's launcher activity. */
    fun build(): List<Intent> =
        listOf(
            Intent(Intent.ACTION_VOICE_COMMAND),
            Intent(Intent.ACTION_ASSIST),
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(GOOGLE_APP_PACKAGE),
        )

    /** Returns the home-screen intent, which dismisses an open assistant overlay. */
    fun home(): Intent =
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
