// Entry point y DI raíz: solo ensambla los módulos.
plugins {
    alias(libs.plugins.tavola.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.android.junit5)
    // google-services: activar al añadir app/google-services.json (Firebase)
}

android {
    namespace = "com.tavola"
    defaultConfig {
        applicationId = "com.tavola"
        versionCode = 1
        versionName = "1.0"
    }

    // Tests de capturas de todo el design system: src/screenshotTest/kotlin, referencias en
    // src/screenshotTestDebug/reference. Viven en :app porque el motor solo resuelve bien las clases R de
    // las dependencias en un módulo de aplicación (en una librería falla con ClassNotFoundException).
    testOptions {
        screenshotTests.create("screenshotTest") {
            engineVersion = libs.versions.screenshot.get()
            targetVariants.clear()
            targetVariants.add("debug")

            dependencies {
                implementation(libs.androidx.compose.ui.tooling)
                implementation(libs.androidx.compose.material.icons.extended)
                implementation(libs.screenshot.validation.api)
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
}

dependencies {
    implementation(projects.domain)
    implementation(projects.data)
    implementation(projects.core.designsystem)
    implementation(projects.feature.calendar)
    // La pantalla de muestra solo existe en debug (ver src/debug/.../TavolaApp.kt).
    debugImplementation(projects.feature.showcase)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
