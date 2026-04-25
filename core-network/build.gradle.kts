plugins {
    id("chimera.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.chimera.network"
}

dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.client.logging)

    implementation(libs.kotlinx.serialization.json)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation("io.ktor:ktor-client-mock:${libs.versions.ktor.get()}")
    testImplementation("io.ktor:ktor-client-content-negotiation:${libs.versions.ktor.get()}")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:${libs.versions.ktor.get()}")
    testImplementation("com.google.truth:truth:${libs.versions.truth.get()}")
}
