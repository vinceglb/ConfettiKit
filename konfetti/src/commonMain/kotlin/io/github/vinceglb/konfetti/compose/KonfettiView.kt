package io.github.vinceglb.konfetti.compose

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import io.github.vinceglb.konfetti.core.Particle
import io.github.vinceglb.konfetti.core.Party
import io.github.vinceglb.konfetti.core.PartySystem
import io.github.vinceglb.konfetti.core.models.CoreRect
import io.github.vinceglb.konfetti.core.models.Shape
import kotlinx.datetime.Clock

@Composable
public fun KonfettiView(
    modifier: Modifier = Modifier,
    parties: List<Party>,
    updateListener: OnParticleSystemUpdateListener? = null,
) {
    lateinit var partySystems: List<PartySystem>

    /**
     * Particles to draw
     */
    val particles = remember { mutableStateOf(emptyList<Particle>()) }

    /**
     * Latest stored frameTimeMilliseconds
     */
    val frameTime = remember { mutableStateOf(0L) }

    /**
     * Area in which the particles are being drawn
     */
    val drawArea = remember { mutableStateOf(CoreRect.CoreRectImpl()) }

    /**
     * Store for drawable images
     */
//    val imageStore = remember { ImageStore() }

    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        partySystems = parties.map {
            PartySystem(
                party = storeImages(
                    it,
                    // imageStore
                ),
                // pixelDensity = Resources.getSystem().displayMetrics.density,
                pixelDensity = density.density,
            )
        }

        while (true) {
            withInfiniteAnimationFrameMillis { frameMs ->
                // Calculate time between frames, fallback to 0 when previous frame doesn't exist
                val deltaMs = if (frameTime.value > 0) (frameMs - frameTime.value) else 0
                frameTime.value = frameMs

                particles.value =
                    partySystems.map { particleSystem ->

                        val totalTimeRunning = getTotalTimeRunning(particleSystem.createdAt)
                        // Do not start particleSystem yet if totalTimeRunning is below delay
                        if (totalTimeRunning < particleSystem.party.delay) return@map listOf()

                        if (particleSystem.isDoneEmitting()) {
                            updateListener?.onParticleSystemEnded(
                                system = particleSystem,
                                activeSystems = partySystems.count { !it.isDoneEmitting() },
                            )
                        }

                        particleSystem.render(deltaMs.div(1000f), drawArea.value)
                    }.flatten()
            }
        }
    }

    Canvas(
        modifier = modifier.onGloballyPositioned {
            drawArea.value = CoreRect.CoreRectImpl(
                x = 0f,
                y = 0f,
                width = it.size.width.toFloat(),
                height = it.size.height.toFloat()
            )
        },
        onDraw = {
            particles.value.forEach { particle ->
                withTransform({
                    rotate(
                        degrees = particle.rotation,
                        pivot =
                        Offset(
                            x = particle.x + (particle.width / 2),
                            y = particle.y + (particle.height / 2),
                        ),
                    )
                    scale(
                        scaleX = particle.scaleX,
                        scaleY = 1f,
                        pivot = Offset(particle.x + (particle.width / 2), particle.y),
                    )
                }) {
                    particle.shape.draw(
                        drawScope = this,
                        particle = particle,
                    )
                }
            }
        },
    )
}

/**
 * Transforms the shapes in the given [Party] object. If a shape is a [Shape.DrawableShape],
 * it replaces the [DrawableImage] with a [ReferenceImage] and stores the [Drawable] in the [ImageStore].
 *
 * @param party The Party object containing the shapes to be transformed.
 * @return A new Party object with the transformed shapes.
 */
internal fun storeImages(
    party: Party,
): Party {
    val transformedShapes = party.shapes.map { shape -> shape }
    return party.copy(shapes = transformedShapes)
}

internal fun getTotalTimeRunning(startTime: Long): Long {
    val currentTime = Clock.System.now().toEpochMilliseconds()
    return (currentTime - startTime)
}
