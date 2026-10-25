// Kotlin puro: sin android.*, Compose ni Firebase.
plugins {
    alias(libs.plugins.tavola.kotlin.jvm)
}

dependencies {
    // api: Flow y LocalDate aparecen en las interfaces públicas del dominio.
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.datetime)
    implementation(libs.javax.inject)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}
