package io.github.vinceglb.confettikit.convention

import org.gradle.api.Project

private const val BASE_PACKAGE = "io.github.vinceglb.confettikit"

val Project.modulePackage: String
    get() = when (path) {
        ":confettikit" -> BASE_PACKAGE
        ":sample:shared" -> "$BASE_PACKAGE.sample.shared"
        else -> "$BASE_PACKAGE${path.replace(":", ".")}".replace("..", ".")
    }

val Project.frameworkBaseName: String
    get() = when (path) {
        ":confettikit" -> "ConfettiKit"
        ":sample:shared" -> "SampleSharedKit"
        else -> name.replaceFirstChar { it.uppercaseChar() }
    }
