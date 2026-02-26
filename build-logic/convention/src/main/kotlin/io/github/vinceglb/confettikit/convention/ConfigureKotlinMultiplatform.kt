package io.github.vinceglb.confettikit.convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKotlinMultiplatform(
    extension: KotlinMultiplatformExtension,
    modulePackage: String,
    frameworkBaseName: String,
) = extension.apply {
    applyDefaultHierarchyTemplate()

    extensions.configure<KotlinMultiplatformAndroidLibraryExtension> {
        configureAndroidKmpLibrary(this, modulePackage = modulePackage)
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            isStatic = true
            baseName = frameworkBaseName
            binaryOption("bundleId", modulePackage)
        }
    }

    jvm()

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets.getByName("commonTest").dependencies {
        implementation(libs.findLibrary("kotlin.test").get())
    }
}
