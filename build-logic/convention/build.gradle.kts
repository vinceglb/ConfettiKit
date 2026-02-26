plugins {
    `kotlin-dsl`
}

group = "io.github.vinceglb.confettikit.convention"

dependencies {
    compileOnly(libs.android.gradleApiPlugin)
    compileOnly(libs.kotlin.multiplatform.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.compose.multiplatform.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("composeMultiplatformLibrary") {
            id = libs.plugins.confettikit.composeMultiplatformLibrary.get().pluginId
            implementationClass = "ComposeMultiplatformLibraryConventionPlugin"
        }
    }
}
