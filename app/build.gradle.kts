// Entry point y DI raíz: solo ensambla los módulos.
plugins {
    alias(libs.plugins.tavola.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.tavola"
    defaultConfig {
        applicationId = "com.tavola"
        // CI passes the run number (-Ptavola.versionCode=N) so every build installs over the previous one.
        versionCode = providers.gradleProperty("tavola.versionCode").map(String::toInt).getOrElse(1)
        // The version lives in the root VERSION file, which is bumped when a release/x.y.z branch is cut.
        versionName = providers.gradleProperty("tavola.versionName").orElse(
            providers.fileContents(rootProject.layout.projectDirectory.file("VERSION")).asText.map(String::trim),
        ).get()
    }

    // Firma estable para las builds de la CI: solo existe si la CI define estas variables de entorno.
    // En local se sigue usando la clave de debug de Android Studio.
    val ciKeystorePath = providers.environmentVariable("TAVOLA_KEYSTORE_PATH").orNull
    if (ciKeystorePath != null) {
        signingConfigs {
            create("ci") {
                storeFile = file(ciKeystorePath)
                storePassword = providers.environmentVariable("TAVOLA_KEYSTORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("TAVOLA_KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("TAVOLA_KEYSTORE_PASSWORD").get()
            }
        }
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
        debug {
            signingConfigs.findByName("ci")?.let { signingConfig = it }
        }
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
    implementation(projects.feature.auth)
    implementation(projects.feature.calendar)
    implementation(projects.feature.groups)
    implementation(projects.feature.recipes)
    implementation(projects.feature.shopping)

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
