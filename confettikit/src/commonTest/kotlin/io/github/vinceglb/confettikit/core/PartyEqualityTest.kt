package io.github.vinceglb.confettikit.core

import io.github.vinceglb.confettikit.core.emitter.Emitter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/**
 * Regression coverage for the `EmitterConfig`-as-data-class refactor: a [Party] built twice
 * from the same arguments must be structurally equal so that re-keying compose effects on a
 * `List<Party>` doesn't fire on every recomposition.
 */
internal class PartyEqualityTest {

    @Test
    fun `two parties built from the same arguments are equal`() {
        val a = Party(emitter = Emitter(1.seconds).max(100))
        val b = Party(emitter = Emitter(1.seconds).max(100))

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `parties differing only in emitter config are not equal`() {
        val a = Party(emitter = Emitter(1.seconds).max(100))
        val b = Party(emitter = Emitter(1.seconds).max(50))

        kotlin.test.assertNotEquals(a, b)
    }

    @Test
    fun `lists of equal parties are equal`() {
        val left = listOf(
            Party(emitter = Emitter(1.seconds).perSecond(30)),
            Party(emitter = Emitter(2.seconds).max(200)),
        )
        val right = listOf(
            Party(emitter = Emitter(1.seconds).perSecond(30)),
            Party(emitter = Emitter(2.seconds).max(200)),
        )

        assertEquals(left, right)
    }
}
