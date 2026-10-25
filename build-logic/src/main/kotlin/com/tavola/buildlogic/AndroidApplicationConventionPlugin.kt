package com.tavola.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

/** Aplicación Android con Compose. SDKs (compile/min/target) vienen del plugin `com.android.settings`. */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<ApplicationExtension> {
                defaultConfig {
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
                compileOptions {
                    sourceCompatibility = javaVersion()
                    targetCompatibility = javaVersion()
                }
                buildFeatures.compose = true
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
