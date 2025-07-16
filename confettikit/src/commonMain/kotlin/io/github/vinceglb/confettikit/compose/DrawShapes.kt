package io.github.vinceglb.confettikit.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import io.github.vinceglb.confettikit.core.Particle
import io.github.vinceglb.confettikit.core.models.Shape

/**
 * Draw a shape to `compose canvas`. Implementations are expected to draw within a square of size
 * `size` and must vertically/horizontally center their asset if it does not have an equal width
 * and height.
 */
internal fun Shape.draw(
    drawScope: DrawScope,
    particle: Particle,
) {
    when (this) {
        Shape.Circle -> {
            val offsetMiddle = particle.width / 2
            drawScope.drawCircle(
                color = Color(particle.color),
                center = Offset(particle.x + offsetMiddle, particle.y + offsetMiddle),
                radius = particle.width / 2,
            )
        }
        Shape.Square -> {
            drawScope.drawRect(
                color = Color(particle.color),
                topLeft = Offset(particle.x, particle.y),
                size = Size(particle.width, particle.height),
            )
        }
        is Shape.Rectangle -> {
            val size = particle.width
            val height = size * heightRatio
            drawScope.drawRect(
                color = Color(particle.color),
                topLeft = Offset(particle.x, particle.y),
                size = Size(size, height),
            )
        }
        is Shape.CustomShape -> {
            val outline = shape.createOutline(
                size = Size(particle.width, particle.height),
                layoutDirection = drawScope.layoutDirection,
                density = drawScope,
            )
            drawScope.translate(particle.x, particle.y) {
                when (outline) {
                    is Outline.Generic -> {
                        drawScope.drawPath(
                            path = outline.path,
                            color = Color(particle.color),
                        )
                    }
                    is Outline.Rectangle -> {
                        drawScope.drawRect(
                            topLeft = outline.rect.topLeft,
                            size = outline.rect.size,
                            color = Color(particle.color),
                        )
                    }
                    is Outline.Rounded -> {
                        val path = Path().apply { addRoundRect(outline.roundRect) }
                        drawScope.drawPath(
                            path = path,
                            color = Color(particle.color),
                        )
                    }
                }
            }
        }
        is Shape.Image -> {
            drawScope.drawImage(
                image = imageBitmap,
                dstSize = IntSize(particle.width.toInt(), particle.height.toInt()),
                dstOffset = IntOffset(particle.x.toInt(), particle.y.toInt()),
            )
        }
        is Shape.Vector -> {
            with(vectorPainter) {
                drawScope.translate(particle.x, particle.y) {
                    draw(
                        size = Size(particle.width, particle.height),
                        colorFilter = ColorFilter.tint(Color(particle.color)),
                    )
                }
            }
        }
    }
}
