package io.github.vinceglb.confettikit.core.models

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
}
