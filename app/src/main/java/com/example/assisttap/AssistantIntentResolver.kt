package com.example.assisttap

import android.content.Intent

/** Answers whether some activity on the device can handle an [Intent]. */
fun interface IntentResolvable {
    /** Returns `true` when [intent] resolves to an activity. */
    fun canResolve(intent: Intent): Boolean
}

/** Picks the first intent that [resolvable] can handle. */
class AssistantIntentResolver(
    private val resolvable: IntentResolvable,
) {
    /** Returns the first resolvable entry of [candidates], or `null` if none resolves. */
    fun resolve(candidates: List<Intent>): Intent? = candidates.firstOrNull(resolvable::canResolve)
}
