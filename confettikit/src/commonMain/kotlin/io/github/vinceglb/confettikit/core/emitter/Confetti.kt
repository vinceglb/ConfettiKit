package io.github.vinceglb.confettikit.core.emitter

import io.github.vinceglb.confettikit.core.models.CoreRect
import io.github.vinceglb.confettikit.core.models.Shape
import io.github.vinceglb.confettikit.core.models.Vector
import kotlin.math.abs
import kotlin.math.pow

/**
 * Confetti holds all data to the current state of the particle
 * Each frame update triggers the `render` method, which recalculates the particle's properties based on its current state.
 *
 * @property location The current position of the particle as a Vector object that contains x and y coordinates.
 * @property color The color of the particle, represented as an integer. (AARRGGBB)
 * @property width The width of the particle in pixels.
 * @property mass The mass of the particle, affecting how forces like gravity influence it. A particle with more mass will move slower under the same force.
 * @property shape The geometric shape of the particle.
 * @property lifespan The duration the particle should be fully visible (opaque) for in milliseconds.
 * @property fadeOut If true, the particle will gradually become transparent after its lifespan has elapsed.
 * @property fadeOutDuration The duration of the fade out in milliseconds.
 * @property acceleration The current acceleration of the particle.
 * @property velocity The current velocity of the particle.
 * @property damping A factor that reduces the particle's velocity over time, simulating air resistance. A higher damping value will slow down the particle faster.
 * @property rotationSpeed3D The speed at which the particle rotates.
 * @property rotationSpeed2D The speed at which the particle rotates in 2D space.
 * @property pixelDensity The pixel density of the device's screen. This is used to ensure that the particle's movement looks consistent across devices with different screen densities.
 */
internal class Confetti(
    var location: Vector,
    private val color: Int,
    val width: Float,
    private val mass: Float,
    val shape: Shape,
    var lifespan: Long = -1L,
    val fadeOut: Boolean = true,
    private val fadeOutDuration: Long = 850,
    private var acceleration: Vector = Vector(0f, 0f),
    var velocity: Vector = Vector(),
    var damping: Float,
    val rotationSpeed3D: Float = 1f,
    val rotationSpeed2D: Float = 1f,
    val pixelDensity: Float,
) {
    companion object {
        private const val DEFAULT_FRAME_RATE = 60f
        private const val GRAVITY = 0.02f
        private const val MAX_ALPHA = 255
        private const val MILLIS_IN_SECOND = 1000
        private const val FULL_CIRCLE = 360f
    }

    var rotation = 0f
    private var rotationWidth = width

    // Expected frame rate
    private var frameRate = DEFAULT_FRAME_RATE
    private var gravity = Vector(0f, GRAVITY)

    var alpha: Int = MAX_ALPHA
    var scaleX = 0f

    /**
     * The color of the particle with the current alpha value applied
     */
    var alphaColor: Int = 0

    /**
     * Determines whether the particle should be drawn.
     * Set to false when the particle moves out of the view
     */
    var drawParticle = true
        private set

    /**
     * Returns the size of the particle in pixels
     */
    fun getSize(): Float = width

    /**
     * Checks if the particle is "dead", i.e., its alpha value has reached 0
     */
    fun isDead(): Boolean = alpha <= 0

    /**
     * Applies a force to the particle, which affects its acceleration
     */
    fun applyForce(force: Vector) {
        acceleration.addScaled(force, 1f / mass)
    }

    /**
     * Updates the state of the particle for each frame of the animation.
     */
    fun render(
        deltaTime: Float,
        drawArea: CoreRect,
    ) {
        applyForce(gravity)
        update(deltaTime, drawArea)
    }

    /**
     * Updates the state of the particle based on its current acceleration, velocity, and location.
     * Also handles the fading out of the particle when its lifespan is over.
     */
    private fun update(
        deltaTime: Float,
        drawArea: CoreRect,
    ) {
        // Calculate time scale, 1f = 60fps
        val timeScale = deltaTime * DEFAULT_FRAME_RATE

        if (location.y > drawArea.height) {
            alpha = 0
            return
        }

        velocity.addScaled(acceleration, timeScale)
        velocity.mult(damping.pow(timeScale))

        location.addScaled(velocity, timeScale * pixelDensity)

        lifespan -= (deltaTime * MILLIS_IN_SECOND).toLong()
        if (lifespan <= 0) updateAlpha(fadeOutElapsed = -lifespan)

        // 2D rotation around the center of the confetti
        rotation += rotationSpeed2D * timeScale
        if (rotation >= FULL_CIRCLE) rotation = 0f

        // 3D rotation effect by decreasing the width and make sure that rotationSpeed is always
        // positive by using abs
        rotationWidth -= abs(rotationSpeed3D) * timeScale
        if (rotationWidth < 0) rotationWidth = width

        scaleX = abs(rotationWidth / width - 0.5f) * 2
        alphaColor = (alpha shl 24) or (color and 0xffffff)

        drawParticle = drawArea.contains(location.x.toInt(), location.y.toInt())
    }

    private fun updateAlpha(fadeOutElapsed: Long) {
        alpha =
            if (fadeOut && fadeOutDuration > 0) {
                val progress = (fadeOutElapsed.toFloat() / fadeOutDuration).coerceIn(0f, 1f)
                (MAX_ALPHA * (1 - progress)).toInt()
            } else {
                0
            }
    }
}
