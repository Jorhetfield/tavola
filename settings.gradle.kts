pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    // Mismo AGP que el catálogo: el plugin de settings centraliza los SDK de todos los módulos.
    val agpVersion = buildscript.sourceFile!!.parentFile.resolve("gradle/libs.versions.toml")
        .readLines().first { it.startsWith("agp =") }.split("\"")[1]
    id("com.android.settings") version agpVersion
}

android {
    compileSdk {
        version = release(37)
    }
    targetSdk {
        version = release(37)
    }
    minSdk {
        version = release(26)
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Tavola"

includeBuild("build-logic")

include(":app")
include(":domain")
include(":data")
include(":feature:calendar")
include(":feature:recipes")
include(":feature:showcase")
include(":core:designsystem")
include(":core:testing")
