plugins {
    id("java-library")
    alias(libs.plugins.kotlin.serialization)
    id("org.jetbrains.kotlin.jvm") version libs.versions.kotlin.get()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    jvmToolchain(8)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
