package io.github.vinceglb.confettikit.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlin.random.Random

/**
 * Creates and remembers a [ConfettiKitState] for use with [ConfettiKit].
 *
 * @param random factory invoked to obtain the [Random] instance handed to each [io.github.vinceglb.confettikit.core.PartySystem].
 * The factory is called once on creation and again on every [ConfettiKitState.reset] so that callers
 * who pass `{ Random(seed) }` get reproducible playback. The default `{ Random.Default }` matches the
 * pre-existing behavior (the singleton [Random] companion is used as-is).
 */
@Composable
public fun rememberConfettiKitState(
    random: () -> Random = { Random.Default },
): ConfettiKitState = remember { ConfettiKitState(random) }

/**
 * Hoisted state for [ConfettiKit] that exposes a more granular API for animation control.
 *
 * Following the same pattern as `LazyListState` / `ScrollState`, this object lets callers:
 * - Inject a deterministic [Random] (via `random` factory) so confetti can be reproduced.
 * - Pause and resume the simulation without unmounting the composable.
 * - Advance the simulation by a manual delta to scrub or freeze a "keyframe".
 * - Reset the timeline back to `t=0`, re-creating particles with a fresh [Random].
 *
 * All mutating methods are safe to call from the UI thread / composition.
 */
@Stable
public class ConfettiKitState internal constructor(
    initialRandomFactory: () -> Random,
) {
    private val pausedState = mutableStateOf(false)
    private val timelineMsState = mutableLongStateOf(0L)
    private var pendingAdvanceMs: Long = 0L

    private var randomFactory: () -> Random = initialRandomFactory

    /**
     * Reset signal observed by the composable as a [androidx.compose.runtime.LaunchedEffect] key.
     * Every increment causes the underlying [io.github.vinceglb.confettikit.core.PartySystem]s to
     * be re-created with a fresh [Random] from the current factory.
     */
    internal val resetSignal = mutableIntStateOf(0)

    /**
     * The [Random] instance currently in use. Replaced on every [reset] by invoking the current
     * random factory (either the one passed to [rememberConfettiKitState] or the latest one set
     * via [useRandom]).
     */
    public var random: Random = randomFactory()
        internal set

    /**
     * Whether the simulation is paused. While paused, real frame deltas are ignored and only
     * manual [advance] calls cause the simulation to progress.
     */
    public var isPaused: Boolean
        get() = pausedState.value
        set(value) {
            pausedState.value = value
        }

    /**
     * Logical elapsed time in milliseconds. Increments only when the simulation advances —
     * i.e., real frame deltas while not paused, plus any [advance] calls.
     */
    public val timelineMs: Long
        get() = timelineMsState.longValue

    /** Pause the simulation. Equivalent to `isPaused = true`. */
    public fun pause() {
        isPaused = true
    }

    /** Resume the simulation. Equivalent to `isPaused = false`. */
    public fun resume() {
        isPaused = false
    }

    /**
     * Queue an additional [deltaMs] milliseconds to be applied on the next frame. The delta is
     * applied regardless of [isPaused], which makes this the primary tool for manual scrubbing
     * while paused.
     *
     * Large deltas are sub-stepped internally by the composable into small chunks so that
     * physics remain stable.
     */
    public fun advance(deltaMs: Long) {
        require(deltaMs >= 0L) { "deltaMs must be non-negative, was $deltaMs" }
        pendingAdvanceMs += deltaMs
    }

    /**
     * Seek the timeline to [targetMs]. The target is compared against the *effective* position
     * `timelineMs + pendingAdvanceMs` — where the timeline will be after the next frame applies
     * any queued advance.
     *
     * - **Forward** (target ≥ effective): queue the delta via [advance].
     * - **Backward but absorbable** (target < effective and the gap fits within the queued
     *   advance): trim [pendingAdvanceMs] in place. This handles rapid scrubbing where slider
     *   events outpace frames — multiple `advanceTo` calls between frames may queue more advance
     *   than the user ultimately wants, and we just shave it back. Nothing is committed yet, so
     *   no reset is needed.
     * - **Backward beyond what's queued** (the user wants to land before [timelineMs]): the
     *   committed timeline can only be wound back by tearing it down, so [reset] and replay
     *   forward to [targetMs]. Particles re-emit from t=0 using the same factory-derived
     *   [Random], which keeps seeded scrubbing deterministic.
     */
    public fun advanceTo(targetMs: Long) {
        require(targetMs >= 0L) { "targetMs must be non-negative, was $targetMs" }
        val effectivePosition = timelineMs + pendingAdvanceMs
        val backward = effectivePosition - targetMs
        when {
            backward <= 0L -> {
                val needed = -backward
                if (needed > 0L) advance(needed)
            }

            backward <= pendingAdvanceMs -> {
                pendingAdvanceMs -= backward
            }

            else -> {
                reset()
                if (targetMs > 0L) advance(targetMs)
            }
        }
    }

    /**
     * Reset the timeline to `0`, drop any pending advance, and signal the composable to
     * re-create its [io.github.vinceglb.confettikit.core.PartySystem]s using a fresh [Random]
     * from the current random factory.
     *
     * For seeded factories like `{ Random(42L) }` this means subsequent playback is identical
     * to the original run.
     */
    public fun reset() {
        pendingAdvanceMs = 0L
        timelineMsState.longValue = 0L
        random = randomFactory()
        resetSignal.intValue++
    }

    /**
     * Replace the random factory and immediately [reset] the simulation. Use this to switch
     * between a seeded `{ Random(seed) }` factory for reproducible playback and
     * `{ Random.Default }` for live randomness without having to recreate the state object.
     */
    public fun useRandom(factory: () -> Random) {
        randomFactory = factory
        reset()
    }

    /**
     * Replace the random factory used by future [reset] calls without resetting the running
     * simulation. The currently-running [random] instance and any in-flight particles keep
     * their existing source — only the *next* [reset] will pull from the new factory.
     *
     * Useful when toggling out of a deterministic-scrubbing mode: you want subsequent restarts
     * to use live randomness, but you don't want the current paused frame to be wiped.
     */
    public fun setRandomFactory(factory: () -> Random) {
        randomFactory = factory
    }

    /** Consume any pending manual advance; returns the value and zeros it out. */
    internal fun takePendingAdvance(): Long {
        val v = pendingAdvanceMs
        pendingAdvanceMs = 0L
        return v
    }

    /** Increment the logical timeline by [deltaMs]. Called by the composable's frame loop. */
    internal fun advanceTimeline(deltaMs: Long) {
        timelineMsState.longValue += deltaMs
    }
}
