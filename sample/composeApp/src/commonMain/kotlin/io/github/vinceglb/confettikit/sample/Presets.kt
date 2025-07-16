package io.github.vinceglb.confettikit.sample

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import confettikit_lib.sample.composeapp.generated.resources.Res
import confettikit_lib.sample.composeapp.generated.resources.confetti
import io.github.vinceglb.confettikit.core.Angle
import io.github.vinceglb.confettikit.core.Party
import io.github.vinceglb.confettikit.core.Position
import io.github.vinceglb.confettikit.core.Spread
import io.github.vinceglb.confettikit.core.emitter.Emitter
import io.github.vinceglb.confettikit.core.models.Shape
import org.jetbrains.compose.resources.imageResource
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class Presets {
    companion object {
        fun explode(): List<Party> {
            return listOf(
                Party(
                    speed = 0f,
                    maxSpeed = 30f,
                    damping = 0.9f,
                    spread = 360,
                    colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                    shapes = listOf(
                        Shape.CustomShape(CircleShape),
                        Shape.CustomShape(HeartShape()),
                        Shape.CustomShape(RoundedCornerShape(5.dp))
                    ),
                    emitter = Emitter(duration = 100.milliseconds).max(100),
                    // position = Position.Relative(0.5, 0.3)
                )
            )
        }

        fun parade(): List<Party> {
            val party = Party(
                speed = 10f,
                maxSpeed = 30f,
                damping = 0.9f,
                angle = Angle.RIGHT - 45,
                spread = Spread.SMALL,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(duration = 5.seconds).perSecond(30),
                position = Position.Relative(0.0, 0.5)
            )

            return listOf(
                party,
                party.copy(
                    angle = party.angle - 90, // flip angle from right to left
                    position = Position.Relative(1.0, 0.5)
                ),
            )
        }

        @Composable
        fun rain(): List<Party> {
            val imageBitmap = imageResource(Res.drawable.confetti)
            val vectorPainter = rememberVectorPainter(Icons.Outlined.Celebration)
            return listOf(
                Party(
                    speed = 0f,
                    maxSpeed = 15f,
                    damping = 0.9f,
                    angle = Angle.BOTTOM,
                    spread = Spread.ROUND,
                    colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                    shapes = listOf(Shape.Image(imageBitmap), Shape.Vector(vectorPainter)),
                    emitter = Emitter(duration = 5.seconds).perSecond(100),
                    position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0))
                )
            )
        }

        @Composable
        fun all() = listOf(
            explode(),
            parade(),
            rain(),
        )
    }
}