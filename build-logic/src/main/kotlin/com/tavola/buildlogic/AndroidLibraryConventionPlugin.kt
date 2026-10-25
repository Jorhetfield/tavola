package com.tavola.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

/** Librería Android sin Compose. SDKs (compile/min) vienen del plugin `com.android.settings`. */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")

            extensions.configure<LibraryExtension> {
                compileOptions {
                    sourceCompatibility = javaVersion()
                    targetCompatibility = javaVersion()
                }
                testOptions {
                    // Necesario para los tests de capturas y para tests que usan recursos/strings.
                    unitTests.isIncludeAndroidResources = true
                }
            }
            addJUnitPlatformLauncher()
            tasks.withType<Test>().configureEach {
                // Un módulo todavía sin tests no debe romper el build.
                failOnNoDiscoveredTests = false
            }
        }
    }
}
