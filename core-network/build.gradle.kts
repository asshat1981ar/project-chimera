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
}
