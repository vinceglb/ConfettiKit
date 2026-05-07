package io.github.vinceglb.confettikit.compose

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class ConfettiKitStateTest {

    @Test
    fun `pause and resume toggle isPaused`() {
        val state = ConfettiKitState { Random.Default }

        assertFalse(state.isPaused)

        state.pause()
        assertTrue(state.isPaused)

        state.resume()
        assertFalse(state.isPaused)
    }

    @Test
    fun `advance accumulates pending delta and is consumed once`() {
        val state = ConfettiKitState { Random.Default }

        state.advance(100L)
        state.advance(50L)

        assertEquals(150L, state.takePendingAdvance())
        assertEquals(0L, state.takePendingAdvance(), "second take should be empty")
    }

    @Test
    fun `advance rejects negative deltas`() {
        val state = ConfettiKitState { Random.Default }
        assertFailsWith<IllegalArgumentException> { state.advance(-1L) }
    }

    @Test
    fun `advanceTimeline updates timelineMs`() {
        val state = ConfettiKitState { Random.Default }

        assertEquals(0L, state.timelineMs)
        state.advanceTimeline(250L)
        assertEquals(250L, state.timelineMs)
        state.advanceTimeline(750L)
        assertEquals(1000L, state.timelineMs)
    }

    @Test
    fun `reset clears timeline and pending advance and bumps reset signal`() {
        val state = ConfettiKitState { Random.Default }

        state.advance(500L)
        state.advanceTimeline(1000L)
        val signalBefore = state.resetSignal.intValue

        state.reset()

        assertEquals(0L, state.timelineMs)
        assertEquals(0L, state.takePendingAdvance())
        assertEquals(signalBefore + 1, state.resetSignal.intValue)
    }

    @Test
    fun `reset re-creates Random via factory`() {
        var calls = 0
        val state = ConfettiKitState {
            calls++
            Random(42L)
        }

        val first = state.random
        assertEquals(1, calls, "factory called once on construction")

        state.reset()

        val second = state.random
        assertEquals(2, calls, "factory called again on reset")
        assertNotSame(first, second, "random instance should be a fresh one")
    }

    @Test
    fun `seeded factory produces deterministic Random sequence after reset`() {
        val state = ConfettiKitState { Random(42L) }

        val firstRun = List(10) { state.random.nextInt() }
        state.reset()
        val secondRun = List(10) { state.random.nextInt() }

        assertEquals(firstRun, secondRun)
    }

    @Test
    fun `default factory yields the singleton Random Default`() {
        val state = ConfettiKitState { Random.Default }
        assertSame(Random.Default, state.random)
    }

    @Test
    fun `advanceTo forward queues pending advance`() {
        val state = ConfettiKitState { Random.Default }

        state.advanceTo(500L)

        assertEquals(0L, state.timelineMs, "timeline only advances on frame, not on advanceTo")
        assertEquals(500L, state.takePendingAdvance())
    }

    @Test
    fun `advanceTo backward triggers reset and queues advance to target`() {
        val state = ConfettiKitState { Random.Default }
        state.advanceTimeline(2000L)

        state.advanceTo(500L)

        assertEquals(0L, state.timelineMs)
        assertEquals(500L, state.takePendingAdvance())
    }

    @Test
    fun `advanceTo rejects negative target`() {
        val state = ConfettiKitState { Random.Default }
        assertFailsWith<IllegalArgumentException> { state.advanceTo(-1L) }
    }

    @Test
    fun `advanceTo at current position queues nothing`() {
        val state = ConfettiKitState { Random.Default }
        state.advanceTimeline(1000L)

        state.advanceTo(1000L)

        assertEquals(1000L, state.timelineMs)
        assertEquals(0L, state.takePendingAdvance())
    }

    @Test
    fun `setRandomFactory swaps factory without resetting`() {
        val state = ConfettiKitState { Random.Default }
        state.advanceTimeline(2000L)
        state.advance(500L)
        val signalBefore = state.resetSignal.intValue

        state.setRandomFactory { Random(7L) }

        assertEquals(2000L, state.timelineMs, "timeline preserved")
        assertEquals(500L, state.takePendingAdvance(), "pending preserved")
        assertEquals(signalBefore, state.resetSignal.intValue, "no reset signal")

        // The next explicit reset pulls from the new factory.
        state.reset()
        val firstSeq = List(5) { state.random.nextInt() }
        state.reset()
        val secondSeq = List(5) { state.random.nextInt() }
        assertEquals(firstSeq, secondSeq, "new factory should be reproducible")
    }

    @Test
    fun `useRandom swaps factory and resets`() {
        val state = ConfettiKitState { Random.Default }
        state.advanceTimeline(2000L)
        state.advance(500L)
        val signalBefore = state.resetSignal.intValue

        state.useRandom { Random(123L) }

        assertEquals(0L, state.timelineMs)
        assertEquals(0L, state.takePendingAdvance())
        assertEquals(signalBefore + 1, state.resetSignal.intValue)

        // Subsequent reset uses the new factory
        val firstSeq = List(5) { state.random.nextInt() }
        state.reset()
        val secondSeq = List(5) { state.random.nextInt() }
        assertEquals(firstSeq, secondSeq, "new factory should produce reproducible sequences")
    }

    @Test
    fun `advanceTo trims pending without resetting when backward fits within queued advance`() {
        // Slider drag fired multiple advanceTo calls between two frames; the latest landed
        // behind the effective position but inside the not-yet-committed queue.
        val state = ConfettiKitState { Random.Default }
        state.advanceTo(1000L) // pending = 1000
        val signalBefore = state.resetSignal.intValue

        state.advanceTo(200L) // backward 800, fits within pending=1000

        assertEquals(0L, state.timelineMs, "timeline untouched")
        assertEquals(
            200L,
            state.takePendingAdvance(),
            "pending trimmed so effective lands at target"
        )
        assertEquals(signalBefore, state.resetSignal.intValue, "no reset signal incremented")
    }

    @Test
    fun `advanceTo trims small backward within pending`() {
        val state = ConfettiKitState { Random.Default }
        state.advanceTo(1000L) // pending = 1000
        val signalBefore = state.resetSignal.intValue

        state.advanceTo(990L) // backward 10, fits

        assertEquals(0L, state.timelineMs)
        assertEquals(990L, state.takePendingAdvance())
        assertEquals(signalBefore, state.resetSignal.intValue)
    }

    @Test
    fun `advanceTo resets when backward exceeds queued advance`() {
        // Once a frame has committed pending into timelineMs, pending=0, and any backward
        // intent must reset — there is no way to undo committed timeline without it.
        val state = ConfettiKitState { Random.Default }
        state.advanceTimeline(1000L) // committed timeline, pending=0
        val signalBefore = state.resetSignal.intValue

        state.advanceTo(900L) // backward 100, pending=0 cannot absorb

        assertEquals(0L, state.timelineMs)
        assertEquals(900L, state.takePendingAdvance())
        assertEquals(signalBefore + 1, state.resetSignal.intValue)
    }

    @Test
    fun `advanceTo to zero from committed timeline always lands at zero`() {
        // Regression: with a fixed-ms tolerance, advancing to 0 from a small committed
        // timelineMs would no-op the trim (pending=0), leaving timelineMs unchanged.
        val state = ConfettiKitState { Random.Default }
        state.advanceTimeline(10L) // small committed timeline, pending=0

        state.advanceTo(0L)

        assertEquals(0L, state.timelineMs, "must hard-land at zero")
        assertEquals(0L, state.takePendingAdvance())
    }
}
