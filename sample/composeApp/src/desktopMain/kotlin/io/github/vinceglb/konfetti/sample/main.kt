package io.github.vinceglb.konfetti.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Konfetti",
    ) {
        App()
    }
}
