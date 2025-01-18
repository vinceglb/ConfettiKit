package io.github.vinceglb.confettikit.core.models

internal sealed interface CoreRect {
    val x: Float
    val y: Float
    val width: Float
    val height: Float

    fun contains(
        px: Int,
        py: Int,
    ): Boolean {
        return px >= x && px <= x + width && py >= y && py <= y + height
    }

    data class CoreRectImpl(
        override val x: Float = 0f,
        override val y: Float = 0f,
        override val width: Float = 0f,
        override val height: Float = 0f,
    ) : CoreRect
}
