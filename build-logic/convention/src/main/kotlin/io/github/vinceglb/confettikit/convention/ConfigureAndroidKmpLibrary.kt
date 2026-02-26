package io.github.vinceglb.confettikit.convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Project

internal fun Project.configureAndroidKmpLibrary(
    extension: KotlinMultiplatformAndroidLibraryExtension,
    modulePackage: String,
) = extension.apply {
    namespace = modulePackage
    minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
    compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    withHostTest {}
}
