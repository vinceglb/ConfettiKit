package io.github.vinceglb.confettikit.core

import io.github.vinceglb.confettikit.core.emitter.BaseEmitter
import io.github.vinceglb.confettikit.core.emitter.Confetti
import io.github.vinceglb.confettikit.core.emitter.PartyEmitter
import io.github.vinceglb.confettikit.core.models.CoreRect
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * PartySystem is responsible for requesting particles from the emitter and updating the particles
 * everytime a new frame is requested.
 * @param party configuration class with instructions on how to create the particles for the Emitter
 * @param createdAt timestamp of when the partySystem is created
 * @param pixelDensity default value taken from resources to measure based on pixelDensity
 * @param random source of randomness used by the emitter for color, shape, size, angle, speed and
 * rotation jitter. Defaults to [Random.Default]; pass a seeded [Random] for deterministic output.
 */
@OptIn(ExperimentalTime::class)
public class PartySystem(
    public val party: Party,
    public val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    pixelDensity: Float,
    random: Random = Random.Default,
) {
    private var enabled = true

    private var emitter: BaseEmitter = PartyEmitter(party.emitter, pixelDensity, random)

    private val activeParticles = mutableListOf<Confetti>()

    // Called every frame to create and update the particles state
    // returns a list of particles that are ready to be rendered
    internal fun render(
        deltaTime: Float,
        drawArea: CoreRect,
    ): List<Particle> {
        activeParticles.forEach { it.render(deltaTime, drawArea) }

        activeParticles.removeAll { it.isDead() }

        if (enabled) {
            activeParticles.addAll(emitter.createConfetti(deltaTime, party, drawArea))
            activeParticles.removeAll { it.isDead() }
        }

        return activeParticles.filter { it.drawParticle }.map { it.toParticle() }
    }

    /**
     * When the emitter is done emitting.
     * @return true if the emitter is done emitting or false when it's still busy or needs to start
     * based on the delay
     */
    internal fun isDoneEmitting(): Boolean =
        (emitter.isFinished() && activeParticles.isEmpty()) || (!enabled && activeParticles.isEmpty())

    internal fun getActiveParticleAmount() = activeParticles.size
}

/**
 * Convert a confetti object to a particle object with instructions on how to draw
 * the confetti to a canvas
 */
internal fun Confetti.toParticle(): Particle {
    return Particle(
        location.x,
        location.y,
        width,
        width,
        alphaColor,
        rotation,
        scaleX,
        shape,
        alpha,
    )
}
