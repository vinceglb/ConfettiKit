package io.github.vinceglb.confettikit.core.emitter

import kotlin.time.Duration

/**
 * Helper class that holds the emission window and produces an [EmitterConfig].
 */
public data class Emitter(
    val duration: Duration,
) {
    /**
     * Spread [amount] particles evenly across the emission window.
     */
    public fun max(amount: Int): EmitterConfig {
        val emittingTimeMs = duration.inWholeMilliseconds
        return EmitterConfig(
            emittingTime = emittingTimeMs,
            amountPerMs = (emittingTimeMs.toFloat() / amount) / 1000f,
        )
    }

    /**
     * Emit [amount] particles per second for the duration of the window.
     */
    public fun perSecond(amount: Int): EmitterConfig {
        return EmitterConfig(
            emittingTime = duration.inWholeMilliseconds,
            amountPerMs = 1f / amount,
        )
    }
}

/**
 * Immutable emitter configuration.
 *
 * Construct via [Emitter.max] or [Emitter.perSecond]; derive variants with [copy]. Being a
 * data class makes any [io.github.vinceglb.confettikit.core.Party] containing this config
 * structurally equatable, which lets [io.github.vinceglb.confettikit.compose.ConfettiKit]
 * skip rebuilding particle systems on recompositions whose parties list is materially
 * unchanged.
 *
 * @property emittingTime maximum time in milliseconds for the emitter to produce particles.
 * A value of `0` means it will continue indefinitely.
 * @property amountPerMs number of seconds between each particle creation. Lower values mean
 * more frequent emissions.
 */
public data class EmitterConfig(
    val emittingTime: Long,
    val amountPerMs: Float,
)
