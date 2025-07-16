package io.github.vinceglb.confettikit.core.models

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.VectorPainter

public sealed interface Shape {
    public data object Circle : Shape {
        // Default replacement for RectF
        internal val rect: CoreRect = CoreRect.CoreRectImpl()
    }

    public data object Square : Shape

    public data class Rectangle(
        /** The ratio of height to width. Must be within range [0, 1] */
        val heightRatio: Float,
    ) : Shape {
        init {
            require(heightRatio in 0f..1f)
        }
    }

    public data class CustomShape(
        val shape: androidx.compose.ui.graphics.Shape,
    ) : Shape

    public data class Image(
        val imageBitmap: ImageBitmap,
    ) : Shape

    public data class Vector(
        val vectorPainter: VectorPainter,
    ) : Shape
}
