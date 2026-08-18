package io.github.vinceglb.confettikit.core

import io.github.vinceglb.confettikit.core.emitter.Emitter
import io.github.vinceglb.confettikit.core.models.CoreRect
import io.github.vinceglb.confettikit.core.models.Shape
import io.github.vinceglb.confettikit.core.models.Size
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

internal class PartySystemRandomTest {
    private val drawArea = CoreRect.CoreRectImpl(width = 1000f, height = 1000f)

    private fun party(): Party = Party(
        emitter = Emitter(1.seconds).perSecond(amount = 30),
        position = Position.Absolute(500f, 500f),
        spread = 360,
        speed = 5f,
        maxSpeed = 20f,
        damping = 0.9f,
        rotation = Rotation.enabled(),
        shapes = listOf(Shape.Square, Shape.Circle),
        colors = listOf(0xff0000, 0x00ff00, 0x0000ff),
        size = listOf(Size.SMALL, Size.MEDIUM, Size.LARGE),
        timeToLive = 5_000,
        fadeOutEnabled = false,
    )

    private fun renderFrames(system: PartySystem, frames: Int, deltaSeconds: Float = 1f / 60f) =
        (0 until frames).flatMap { system.render(deltaSeconds, drawArea) }

    @Test
    fun `same seeded Random produces identical particle output`() {
        val systemA = PartySystem(party = party(), pixelDensity = 1f, random = Random(42L))
        val systemB = PartySystem(party = party(), pixelDensity = 1f, random = Random(42L))

        val a = renderFrames(systemA, frames = 30)
        val b = renderFrames(systemB, frames = 30)

        assertTrue(a.isNotEmpty(), "expected particles to be produced")
        assertEquals(a, b, "same seed should produce identical particle output")
    }

    @Test
    fun `different seeds produce diverging output`() {
        val systemA = PartySystem(party = party(), pixelDensity = 1f, random = Random(1L))
        val systemB = PartySystem(party = party(), pixelDensity = 1f, random = Random(99999L))

        val a = renderFrames(systemA, frames = 60)
        val b = renderFrames(systemB, frames = 60)

        assertTrue(a.isNotEmpty(), "expected particles to be produced for seed A")
        assertTrue(b.isNotEmpty(), "expected particles to be produced for seed B")
        assertNotEquals(a, b, "different seeds should produce different particle output")
    }
}
