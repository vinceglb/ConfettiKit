package io.github.vinceglb.confettikit.sample

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.window.ComposeViewport
import confettikit_lib.sample.composeapp.generated.resources.Res
import confettikit_lib.sample.composeapp.generated.resources.noto_color_emoji
import org.jetbrains.compose.resources.getFontResourceBytes
import org.jetbrains.compose.resources.rememberResourceEnvironment

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        val resourceEnvironment = rememberResourceEnvironment()
        val fontFamilyResolver = LocalFontFamilyResolver.current
        var loaded by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            val bytes = getFontResourceBytes(resourceEnvironment, Res.font.noto_color_emoji)
            val fontFamily = FontFamily(listOf(Font("NotoColorEmoji", bytes)))
            fontFamilyResolver.preload(fontFamily)
            loaded = true
        }

        if (loaded) {
            App()
        }
    }
}
