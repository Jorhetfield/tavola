plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePluginApi)
    compileOnly(libs.kotlin.gradlePlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.tavola.android.application.get().pluginId
            implementationClass = "com.tavola.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.tavola.android.library.get().pluginId
            implementationClass = "com.tavola.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidComposeLibrary") {
            id = libs.plugins.tavola.android.compose.library.get().pluginId
            implementationClass = "com.tavola.buildlogic.AndroidComposeLibraryConventionPlugin"
        }
        register("kotlinJvm") {
            id = libs.plugins.tavola.kotlin.jvm.get().pluginId
            implementationClass = "com.tavola.buildlogic.KotlinJvmConventionPlugin"
        }
    }
}
