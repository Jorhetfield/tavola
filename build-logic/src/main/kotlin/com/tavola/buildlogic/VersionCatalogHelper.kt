package com.tavola.buildlogic

import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

private fun Project.libs() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.getVersionByName(name: String): String = libs().findVersion(name)
    .orElseThrow { GradleException("Could not find a version for `$name` in libs.versions.toml") }
    .requiredVersion

/** Gradle 9 exige el launcher de JUnit Platform en el runtime de test (misma versión que Jupiter). */
internal fun Project.addJUnitPlatformLauncher() {
    dependencies.add(
        "testRuntimeOnly",
        "org.junit.platform:junit-platform-launcher:${getVersionByName("junitJupiter")}",
    )
}

/** Versión de Java compartida por todos los módulos (`javaVersion` en el catálogo). */
internal fun Project.javaVersion(): JavaVersion = JavaVersion.toVersion(getVersionByName("javaVersion"))
