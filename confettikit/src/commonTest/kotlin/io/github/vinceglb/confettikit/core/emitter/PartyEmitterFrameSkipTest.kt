package io.github.vinceglb.confettikit.core.emitter

import io.github.vinceglb.confettikit.core.Party
import io.github.vinceglb.confettikit.core.PartySystem
import io.github.vinceglb.confettikit.core.Position
import io.github.vinceglb.confettikit.core.Rotation
import io.github.vinceglb.confettikit.core.models.CoreRect
import io.github.vinceglb.confettikit.core.models.Shape
import io.github.vinceglb.confettikit.core.models.Size
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class PartyEmitterFrameSkipTest {
    private val drawArea = CoreRect.CoreRectImpl(width = 1000f, height = 1000f)

    private fun party(emitterConfig: EmitterConfig): Party =
        Party(
            emitter = emitterConfig,
            position = Position.Absolute(0f, 0f),
            spread = 0,
            speed = 0f,
            maxSpeed = -1f,
            damping = 1f,
            rotation = Rotation.disabled(),
            shapes = listOf(Shape.Square),
            colors = listOf(0xffffff),
            size = listOf(Size(sizeInDp = 8, mass = 5f, massVariance = 0f)),
            timeToLive = 10_000,
            fadeOutEnabled = false,
        )

    @Test
    fun createsParticlesWithDifferentAgesWhenDeltaIsLarge() {
        val emitterConfig = Emitter(1.seconds).perSecond(amount = 10)
        val emitter = PartyEmitter(emitterConfig = emitterConfig, pixelDensity = 1f)

        val created = emitter.createConfetti(deltaTime = 0.25f, party = party(emitterConfig), drawArea = drawArea)

        assertEquals(2, created.size)
        val ys = created.map { it.location.y }
        assertTrue(ys[0] > ys[1], "expected older particle to have moved further (y0=${ys[0]}, y1=${ys[1]})")
    }

    @Test
    fun particleSpawnedAtFrameEndIsNotAdvanced() {
        val emitterConfig = Emitter(1.seconds).perSecond(amount = 10)
        val system = PartySystem(party = party(emitterConfig), pixelDensity = 1f)

        val particles = system.render(deltaTime = 0.1f, drawArea = drawArea)

        assertEquals(1, particles.size)
        assertEquals(0f, particles[0].x)
        assertEquals(0f, particles[0].y)
    }
}

