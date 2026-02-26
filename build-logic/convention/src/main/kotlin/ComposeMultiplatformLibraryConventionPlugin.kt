import io.github.vinceglb.confettikit.convention.configureKotlinMultiplatform
import io.github.vinceglb.confettikit.convention.frameworkBaseName
import io.github.vinceglb.confettikit.convention.libs
import io.github.vinceglb.confettikit.convention.modulePackage
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ComposeMultiplatformLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.findPlugin("androidKotlinMultiplatformLibrary").get().get().pluginId)
                apply(libs.findPlugin("kotlinMultiplatform").get().get().pluginId)
                apply(libs.findPlugin("composeMultiplatform").get().get().pluginId)
                apply(libs.findPlugin("composeCompiler").get().get().pluginId)
            }

            extensions.configure<KotlinMultiplatformExtension> {
                configureKotlinMultiplatform(
                    extension = this,
                    modulePackage = modulePackage,
                    frameworkBaseName = frameworkBaseName,
                )
            }
        }
    }
}
