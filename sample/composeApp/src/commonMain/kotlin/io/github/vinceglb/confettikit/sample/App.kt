package io.github.vinceglb.confettikit.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
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
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import io.github.vinceglb.confettikit.compose.ConfettiKit
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

@Composable
fun App() {
    var isAnimating by remember { mutableStateOf(false) }
    var index by remember { mutableStateOf(0) }
    var simulateLag by remember { mutableStateOf(false) }

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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Hello ConfettiKit!")

                Button(
                    enabled = !isAnimating,
                    onClick = { isAnimating = !isAnimating },
                ) {
                    Text("Fiesta 🥳")
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
            }

            if (isAnimating) {
                val presets = Presets.all()
                ConfettiKit(
                    modifier = Modifier.fillMaxSize(),
                    parties = presets[index],
                    onParticleSystemEnded = { _, activeSystems ->
                        if (activeSystems == 0 && isAnimating) {
                            isAnimating = false
                            index = (index + 1) % presets.size
                        }
                    },
                )
            }
        }
    }
}
