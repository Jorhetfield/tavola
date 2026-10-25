// Fakes y utilidades de test compartidas. Módulo JVM: lo usan tanto tests JVM como unit tests de Android.
// Uso desde otro módulo: testImplementation(projects.core.testing)
plugins {
    alias(libs.plugins.tavola.kotlin.jvm)
}

dependencies {
    api(projects.domain)
    api(libs.kotlinx.coroutines.test)
    api(libs.junit.jupiter)
    api(libs.turbine)

    testRuntimeOnly(libs.junit.jupiter.engine)
}
