package io.github.vinceglb.confettikit.core.emitter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val TOLERANCE = 0.00000000001f

internal class EmitterTest {

    @Test
    fun `when duration is 1 s and max amount is 1 then amountPerMs should be 1`() {
        val config = Emitter(duration = 1.seconds).max(amount = 1)

        assertEquals(expected = 1_000, actual = config.emittingTime)
        assertEquals(expected = 1f, actual = config.amountPerMs, absoluteTolerance = TOLERANCE)
    }

    @Test
    fun `when duration is 100 ms and max amount is 1_000 then amountPerMs should be 1E-4`() {
        val config = Emitter(duration = 100.milliseconds).max(amount = 1_000)

        assertEquals(expected = 100, actual = config.emittingTime)
        assertEquals(expected = 0.0001f, actual = config.amountPerMs, absoluteTolerance = TOLERANCE)
    }

    @Test
    fun `when duration is 100 ms and amount per second is 1 then amountPerMs should be 1`() {
        val config = Emitter(duration = 100.milliseconds).perSecond(amount = 1)

        assertEquals(expected = 100, actual = config.emittingTime)
        assertEquals(expected = 1f, actual = config.amountPerMs, absoluteTolerance = TOLERANCE)
    }

    @Test
    fun `when duration is 100 ms and amount per second is 10_000 then amountPerMs should be 1E-4`() {
        val config = Emitter(duration = 100.milliseconds).perSecond(amount = 10_000)

        assertEquals(expected = 100, actual = config.emittingTime)
        assertEquals(expected = 0.0001f, actual = config.amountPerMs, absoluteTolerance = TOLERANCE)
    }
}
