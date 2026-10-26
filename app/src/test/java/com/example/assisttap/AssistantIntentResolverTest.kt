package com.example.assisttap

import android.content.Intent
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class AssistantIntentResolverTest {
    private val first = mockk<Intent>()
    private val second = mockk<Intent>()
    private val third = mockk<Intent>()
    private val candidates = listOf(first, second, third)

    @Test
    fun `returns first candidate when all resolvable`() {
        assertSame(first, AssistantIntentResolver { true }.resolve(candidates))
    }

    @Test
    fun `skips unresolvable candidates in order`() {
        val checked = mutableListOf<Intent>()
        val resolver =
            AssistantIntentResolver {
                checked += it
                it === third
            }

        assertSame(third, resolver.resolve(candidates))
        assert(checked == candidates)
    }

    @Test
    fun `returns null when none resolvable`() {
        assertNull(AssistantIntentResolver { false }.resolve(candidates))
    }
}
