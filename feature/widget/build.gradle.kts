// Home-screen widgets (Jetpack Glance). Depends on :domain and :core:designsystem (for the palette), never on another feature:
// `:app` hands it the way to open screens through WidgetLauncher.
plugins {
    alias(libs.plugins.tavola.android.compose.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.android.junit5)
}

android {
    namespace = "com.tavola.feature.widget"
}

dependencies {
    implementation(projects.domain)
    implementation(projects.core.designsystem)
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.work.runtime)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)

    testImplementation(projects.core.testing)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.kotlinx.coroutines.test)
}
