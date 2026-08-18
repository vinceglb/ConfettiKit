package io.github.vinceglb.confettikit.compose

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.vinceglb.confettikit.core.Particle
import io.github.vinceglb.confettikit.core.Party
import io.github.vinceglb.confettikit.core.PartySystem
import io.github.vinceglb.confettikit.core.models.CoreRect
import io.github.vinceglb.confettikit.core.models.Shape

private const val SUB_STEP_MS = 16L

@Composable
public fun ConfettiKit(
    modifier: Modifier = Modifier,
    parties: List<Party>,
    state: ConfettiKitState = rememberConfettiKitState(),
    onParticleSystemStarted: (PartySystem, Int) -> Unit = { _, _ -> },
    onParticleSystemEnded: (PartySystem, Int) -> Unit = { _, _ -> },
) {
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

    val density = LocalDensity.current

    // Capture the latest callbacks so the long-running frame loop always invokes the most
    // recent versions even when the caller rebuilds them across compositions.
    val currentOnStarted by rememberUpdatedState(onParticleSystemStarted)
    val currentOnEnded by rememberUpdatedState(onParticleSystemEnded)

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        frameTime.value = 0L
    }
    // Keying on `parties` makes the frame loop relaunch with the freshly-captured list when
    // the caller swaps presets — without this, rapid taps could rebuild against a stale value
    // because reads from a State object updated by the same recomposition can race against
    // the frame callback. Callers should pass a stable list (the sample uses `remember(index)`)
    // since `EmitterConfig` and custom shapes may rely on referential equality. Reset-driven
    // rebuilds (state.reset()) are handled inside the loop on the resetSignal so scrubbing
    // doesn't tear down the coroutine.
    LaunchedEffect(state, parties) {
        fun createPartySystems(): List<PartySystem> = parties.map { party ->
            PartySystem(
                party = storeImages(party),
                pixelDensity = density.density,
                random = state.random,
            )
        }

        var partySystems = createPartySystems()
        // All systems in a parties list are created together. Delays are measured from this
        // shared logical timestamp, including when a new preset replaces one already running.
        var systemsStartTimelineMs = state.timelineMs
        // To prevent multiple callbacks for the same event on each frame
        val startedSystems = mutableSetOf<PartySystem>()
        val endedSystems = mutableSetOf<PartySystem>()
        var lastResetSignal = state.resetSignal.intValue

        frameTime.value = 0L
        // Drop any frame still painted from a previous preset so the swap doesn't briefly
        // show stale particles on top of the freshly-built systems.
        particles.value = emptyList()

        while (true) {
            withInfiniteAnimationFrameMillis { frameMs ->
                val signal = state.resetSignal.intValue
                if (signal != lastResetSignal) {
                    partySystems = createPartySystems()
                    systemsStartTimelineMs = state.timelineMs
                    startedSystems.clear()
                    endedSystems.clear()
                    particles.value = emptyList()
                    lastResetSignal = signal
                    frameTime.value = 0L
                }

                val realDeltaMs = if (frameTime.value > 0) (frameMs - frameTime.value) else 0L
                frameTime.value = frameMs

                val effectiveMs = state.takeEffectiveAdvance(realDeltaMs)

                if (effectiveMs <= 0L) {
                    // No time advance; particles list is unchanged. Drawing the previous list
                    // freezes the current visual state.
                    return@withInfiniteAnimationFrameMillis
                }

                // Sub-step large effective deltas (e.g. a manual seek) into ~16 ms chunks so
                // that physics, damping and emission timing stay consistent with normal playback.
                // Track the timeline locally and publish ONCE per frame at the end — a 1 s scrub
                // would otherwise produce ~62 snapshot writes to `timelineMs`, invalidating
                // every state reader (slider, label, etc.) once per sub-step.
                val startTimelineMs = state.timelineMs
                var localTimelineMs = startTimelineMs
                var remaining = effectiveMs
                var lastParticles: List<Particle> = emptyList()
                while (remaining > 0L) {
                    val stepMs = remaining.coerceAtMost(SUB_STEP_MS)
                    localTimelineMs += stepMs
                    remaining -= stepMs
                    lastParticles = partySystems.map { particleSystem ->
                        val totalTimeRunning = localTimelineMs - systemsStartTimelineMs
                        if (totalTimeRunning < particleSystem.party.delay) return@map listOf()

                        if (particleSystem !in startedSystems) {
                            val activeCount = partySystems.count {
                                totalTimeRunning >= it.party.delay && !it.isDoneEmitting()
                            }
                            currentOnStarted(particleSystem, activeCount)
                            startedSystems.add(particleSystem)
                        }

                        if (particleSystem.isDoneEmitting()) {
                            if (particleSystem !in endedSystems) {
                                currentOnEnded(
                                    particleSystem,
                                    partySystems.count { !it.isDoneEmitting() },
                                )
                                endedSystems.add(particleSystem)
                            }
                        }

                        val activeStepMs = postDelayStepMs(
                            totalTimeRunningMs = totalTimeRunning,
                            delayMs = particleSystem.party.delay.toLong(),
                            stepMs = stepMs,
                        )
                        particleSystem.render(activeStepMs / 1000f, drawArea.value)
                    }.flatten()
                }
                state.advanceTimeline(localTimelineMs - startTimelineMs)
                particles.value = lastParticles
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
        },
    )
}

/** Return how much of the current simulation step occurred after a party's delay elapsed. */
internal fun postDelayStepMs(
    totalTimeRunningMs: Long,
    delayMs: Long,
    stepMs: Long,
): Long = (totalTimeRunningMs - delayMs).coerceIn(0L, stepMs)

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
