package io.github.vinceglb.confettikit.compose

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.vinceglb.confettikit.core.Particle
import io.github.vinceglb.confettikit.core.Party
import io.github.vinceglb.confettikit.core.PartySystem
import io.github.vinceglb.confettikit.core.models.CoreRect
import io.github.vinceglb.confettikit.core.models.Shape
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Stable
private class ConfettiState(
    parties: List<Party>,
    density: Density,
    private val onParticleSystemStarted: (PartySystem, Int) -> Unit = { _, _ -> },
    private val onParticleSystemEnded: (PartySystem, Int) -> Unit = { _, _ -> },
) {

    private val partySystems: List<PartySystem> = parties.map {
        PartySystem(
            party = storeImages(it),
            pixelDensity = density.density,
        )
    }

    private val startedSystems = mutableSetOf<PartySystem>()
    private val endedSystems = mutableSetOf<PartySystem>()

    /**
     * Area in which the particles are being drawn
     */
    var drawArea by mutableStateOf(CoreRect.CoreRectImpl())

    /**
     * Particles to draw
     */
    var particles by mutableStateOf<List<Particle>>(emptyList())
        private set

    /**
     * Latest stored frameTimeMilliseconds
     */
    private var frameTime by mutableStateOf(0L)


    suspend fun launch() {
        startedSystems.clear()
        endedSystems.clear()

        while (true) {
            withInfiniteAnimationFrameMillis { frameMs ->
                // Calculate time between frames, fallback to 0 when previous frame doesn't exist
                val deltaMs = if (frameTime > 0) (frameMs - frameTime) else 0
                frameTime = frameMs

                particles = partySystems.map { particleSystem ->
                    val totalTimeRunning = getTotalTimeRunning(particleSystem.createdAt)
                    // Do not start particleSystem yet if totalTimeRunning is below delay
                    if (totalTimeRunning < particleSystem.party.delay) return@map listOf()

                    if (particleSystem !in startedSystems) {
                        onParticleSystemStarted(
                            particleSystem,
                            partySystems.count {
                                getTotalTimeRunning(it.createdAt) >= it.party.delay && !it.isDoneEmitting()
                            },
                        )
                        startedSystems.add(particleSystem)
                    }

                    if (particleSystem.isDoneEmitting()) {
                        if (particleSystem !in endedSystems) {
                            onParticleSystemEnded(
                                particleSystem,
                                partySystems.count { !it.isDoneEmitting() },
                            )
                            endedSystems.add(particleSystem)
                        }
                    }

                    particleSystem.render(deltaMs.div(1000f), drawArea)
                }.flatten()
            }
        }
    }

    fun onResume() {
        frameTime = 0L
    }

    /**
     * Transforms the shapes in the given [Party] object. If a shape is a [Shape.DrawableShape],
     * it replaces the [DrawableImage] with a [ReferenceImage] and stores the [Drawable] in the [ImageStore].
     *
     * @param party The Party object containing the shapes to be transformed.
     * @return A new Party object with the transformed shapes.
     */
    private fun storeImages(
        party: Party,
    ): Party {
        val transformedShapes = party.shapes.map { shape -> shape }
        return party.copy(shapes = transformedShapes)
    }

    @OptIn(ExperimentalTime::class)
    private fun getTotalTimeRunning(startTime: Long): Long {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        return (currentTime - startTime)
    }
}

@Composable
private fun rememberConfettiState(
    parties: List<Party>,
    onParticleSystemStarted: (PartySystem, Int) -> Unit = { _, _ -> },
    onParticleSystemEnded: (PartySystem, Int) -> Unit = { _, _ -> },
): ConfettiState {
    val density = LocalDensity.current
    val state = remember(parties, density, onParticleSystemStarted, onParticleSystemEnded) {
        ConfettiState(parties, density, onParticleSystemStarted, onParticleSystemEnded)
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        state.onResume()
    }

    LaunchedEffect(state) {
        state.launch()
    }

    return state
}

private fun DrawScope.drawParticles(
    state: ConfettiState,
) {
    state.particles.forEach { particle ->
        withTransform({
            rotate(
                degrees = particle.rotation,
                pivot = Offset(
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
}

/**
 * Composable that displays confetti particles based on the provided [parties] configurations.
 *
 * @param modifier Modifier to be applied to the Canvas.
 * @param parties List of [Party] configurations for the confetti.
 * @param onParticleSystemStarted Callback invoked when a particle system starts emitting particles.
 * @param onParticleSystemEnded Callback invoked when a particle system ends emitting particles.
 */
@Composable
public fun ConfettiKit(
    modifier: Modifier = Modifier,
    parties: List<Party>,
    onParticleSystemStarted: (PartySystem, Int) -> Unit = { _, _ -> },
    onParticleSystemEnded: (PartySystem, Int) -> Unit = { _, _ -> },
) {
    val confettiState = rememberConfettiState(
        parties = parties,
        onParticleSystemStarted = onParticleSystemStarted,
        onParticleSystemEnded = onParticleSystemEnded,
    )

    Canvas(
        modifier = modifier.onGloballyPositioned {
            confettiState.drawArea = CoreRect.CoreRectImpl(
                x = 0f,
                y = 0f,
                width = it.size.width.toFloat(),
                height = it.size.height.toFloat()
            )
        },
        onDraw = {
            drawParticles(confettiState)
        },
    )
}

/**
 * Modifier that draws confetti particles over the content it's applied to.
 *
 * @param parties List of [Party] configurations for the confetti.
 * @param drawArea Lambda to customize the drawing area size based on the composable's size.
 * @param onParticleSystemStarted Callback invoked when a particle system starts emitting particles.
 * @param onParticleSystemEnded Callback invoked when a particle system ends emitting particles.
 * @return A [Modifier] that draws confetti particles.
 */
@Composable
public fun Modifier.drawConfetti(
    parties: List<Party>,
    drawArea: (IntSize) -> IntSize = { it },
    onParticleSystemStarted: (PartySystem, Int) -> Unit = { _, _ -> },
    onParticleSystemEnded: (PartySystem, Int) -> Unit = { _, _ -> },
): Modifier = composed {

    val confettiState = rememberConfettiState(
        parties = parties,
        onParticleSystemStarted = onParticleSystemStarted,
        onParticleSystemEnded = onParticleSystemEnded,
    )

    this
        .onGloballyPositioned {
            val size = drawArea(it.size)
            confettiState.drawArea = CoreRect.CoreRectImpl(
                x = 0f,
                y = 0f,
                width = size.width.toFloat(),
                height = size.height.toFloat()
            )
        }
        .drawWithContent {
            drawContent()
            drawParticles(confettiState)
        }
}