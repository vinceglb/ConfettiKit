package io.github.vinceglb.confettikit.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.confettikit.compose.ConfettiKit

@Composable
fun App() {
    var isAnimating by remember { mutableStateOf(false) }
    var index by remember { mutableStateOf(0) }

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
                    Text("Fiesta \uD83E\uDD73")
                }
            }

            if (isAnimating) {
                ConfettiKit(
                    modifier = Modifier.fillMaxSize(),
                    parties = Presets.all[index],
                    onParticleSystemEnded = { _, activeSystems ->
                        if (activeSystems == 0 && isAnimating) {
                            isAnimating = false
                            index = (index + 1) % Presets.all.size
                        }
                    },
                )
            }
        }
    }
}
