// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Plugins de terceros: solo se declaran aquí (apply false) para que build-logic los pueda aplicar.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.android.junit5) apply false
    alias(libs.plugins.spotless)
}

// Formato con ktlint para todo el repo (reglas en .editorconfig). `spotlessApply` formatea, `spotlessCheck` verifica.
spotless {
    lineEndings = com.diffplug.spotless.LineEnding.UNIX
    kotlin {
        target(
            fileTree(rootDir) {
                include("**/src/**/*.kt")
                exclude("**/build/**", "**/.gradle/**")
            },
        )
        ktlint(libs.versions.ktlint.get())
    }
    kotlinGradle {
        target(
            fileTree(rootDir) {
                include("*.gradle.kts", "*/*.gradle.kts", "*/*/*.gradle.kts", "settings.gradle.kts")
                exclude("**/build/**", "**/.gradle/**")
            },
        )
        ktlint(libs.versions.ktlint.get())
    }
}
