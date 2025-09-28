package io.github.vinceglb.confettikit.core.emitter

import kotlin.time.Duration

/**
 * Emitter class that holds the duration that the emitter will create confetti particles
 */
public data class Emitter(
    val duration: Duration,
) {
    /**
     * Max amount of particles that will be created over the duration that is set
     */
    public fun max(amount: Int): EmitterConfig = EmitterConfig(this).max(amount)

    /**
     * Amount of particles that will be created per second
     */
    public fun perSecond(amount: Int): EmitterConfig = EmitterConfig(this).perSecond(amount)
}

/**
 * EmitterConfig class that will gold the Emitter configuration and amount of particles that
 * will be created over certain time
 */
public class EmitterConfig(
    emitter: Emitter,
) {
    /** Max time allowed to emit in milliseconds */
    public var emittingTime: Long = 0

    /** Amount of time needed for each particle creation in milliseconds */
    public var amountPerMs: Float = 0f

    init {
        this.emittingTime = emitter.duration.inWholeMilliseconds
    }

    /**
     * Amount of particles created over the duration that is set
     */
    public fun max(amount: Int): EmitterConfig {
        this.amountPerMs = (emittingTime.toFloat() / amount) / 1000f
        return this
    }

    /**
     * Amount of particles that will be created per second
     */
    public fun perSecond(amount: Int): EmitterConfig {
        this.amountPerMs = 1f / amount
        return this
    }
}
