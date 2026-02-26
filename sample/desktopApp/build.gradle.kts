import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
}

compose.desktop {
    application {
        mainClass = "io.github.vinceglb.confettikit.sample.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "io.github.vinceglb.confettikit.sample"
            packageVersion = "1.0.0"
        }
    }
}

dependencies {
    implementation(projects.sample.shared)
    implementation(compose.desktop.currentOs)
}
