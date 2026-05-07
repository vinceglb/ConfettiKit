package io.github.vinceglb.confettikit.sample

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.confettikit.compose.ConfettiKit
import io.github.vinceglb.confettikit.compose.rememberConfettiKitState
import io.github.vinceglb.confettikit.core.Party
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

private const val REPLAY_SEED = 1234L

/**
 * Time at which every particle in this preset has finished fading: the latest of
 * `delay + emitter.emittingTime + timeToLive (+ fadeOutDuration)` across all parties.
 */
private fun List<Party>.expectedDurationMs(): Long = maxOfOrNull { party ->
    party.delay.toLong() +
            party.emitter.emittingTime +
            party.timeToLive +
            (if (party.fadeOutEnabled) party.fadeOutDuration else 0L)
} ?: 0L

@Composable
fun App() {
    var isAnimating by remember { mutableStateOf(false) }
    var index by remember { mutableStateOf(0) }
    var simulateLag by remember { mutableStateOf(false) }
    var manualControl by remember { mutableStateOf(false) }
    var scrubMs by remember { mutableStateOf(0f) }

    val state = rememberConfettiKitState()
    val presets = Presets.all()
    // Cache the parties list per index so the reference is stable across recompositions.
    // Without this, any non-stable Shape (e.g. a `class` with no equals override) would make
    // each composition's parties list structurally unequal to the previous one, re-keying
    // ConfettiKit's LaunchedEffect on every state change and visibly restarting the animation.
    val currentParties = remember(index) { presets[index].second }
    val animationDurationMs = remember(index) {
        presets[index].second.expectedDurationMs().coerceAtLeast(1L)
    }

    // Manual control freezes whatever frame is currently on screen and exposes the slider.
    // Both toggles use setRandomFactory (not useRandom) so the simulation is *not* reset on
    // toggle — the live frame stays where it was. Determinism kicks in on the next explicit
    // restart (preset button), at which point the queued seeded factory takes effect.
    // Scrubbing backward also re-instantiates Random via the factory, so once the user drags
    // back through any earlier timestamp the run becomes seeded from there onward.
    LaunchedEffect(manualControl) {
        if (manualControl) {
            state.setRandomFactory { Random(REPLAY_SEED) }
            state.pause()
        } else {
            state.setRandomFactory { Random.Default }
            state.resume()
        }
    }

    if (simulateLag) {
        LaunchedEffect(Unit) {
            while (true) {
                withInfiniteAnimationFrameMillis {
                    val start = TimeSource.Monotonic.markNow()
                    while (start.elapsedNow() < 40.milliseconds) {
                        // Busy-loop to simulate main-thread jank
                    }
                }
            }
        }
    }

    Scaffold {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Hello ConfettiKit!")

                presets.forEachIndexed { i, (name, _) ->
                    Button(
                        onClick = {
                            // Switch to this preset, reset the simulation, snap the slider
                            // back to 0, and mount the canvas. Clicking again restarts the
                            // same preset; clicking another swaps to it (ConfettiKit re-keys
                            // its frame loop on `parties`).
                            index = i
                            state.reset()
                            scrubMs = 0f
                            isAnimating = true
                        },
                    ) {
                        Text(name)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Simulate lag")
                    Switch(
                        checked = simulateLag,
                        onCheckedChange = { simulateLag = it },
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Manual control")
                    Switch(
                        checked = manualControl,
                        onCheckedChange = { enabled ->
                            // Sync scrubMs to live playback BEFORE flipping manualControl, so
                            // the displayValueMs computation in the same recomposition reads
                            // the current position. Otherwise the slider snaps to the stale
                            // scrubMs (often 0) and only catches up after the LaunchedEffect
                            // runs async — visible as a back-and-forth jump on toggle on.
                            if (enabled) {
                                scrubMs = state.timelineMs.toFloat()
                                    .coerceIn(0f, animationDurationMs.toFloat())
                            }
                            manualControl = enabled
                        },
                    )
                }

                // Always render the scrub controls. `enabled = manualControl` makes the slider
                // non-interactive (and visually dimmed) when manual control is off, while
                // keeping the layout stable. While disabled, bind the thumb to the live
                // `state.timelineMs` so it tracks playback; while enabled, bind to `scrubMs`
                // so the thumb follows the user's gesture without one-frame lag.
                val displayValueMs = if (manualControl) {
                    scrubMs
                } else {
                    state.timelineMs.toFloat().coerceIn(0f, animationDurationMs.toFloat())
                }
                Slider(
                    value = displayValueMs,
                    onValueChange = { value ->
                        scrubMs = value
                        state.advanceTo(value.toLong())
                    },
                    enabled = manualControl,
                    valueRange = 0f..animationDurationMs.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(horizontal = 32.dp),
                )
                Text("${displayValueMs.toLong()} / $animationDurationMs ms")
            }

            if (isAnimating) {
                ConfettiKit(
                    modifier = Modifier.fillMaxSize(),
                    parties = currentParties,
                    state = state,
                    onParticleSystemEnded = { _, activeSystems ->
                        if (activeSystems == 0 && isAnimating && !manualControl) {
                            isAnimating = false
                        }
                    },
                )
            }
        }
    }
}
