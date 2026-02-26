plugins {
    alias(libs.plugins.confettikit.composeMultiplatformLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.confettikit)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

    androidLibrary {
        androidResources.enable = true
    }
}
