plugins {
    id("chimera.android.library")
    alias(libs.plugins.hilt)
    id("kotlin-kapt")
}

android {
    namespace = "com.chimera.domain"
}

dependencies {
    implementation(project(":core-model"))
    implementation(project(":core-data"))
    implementation(project(":core-database"))
    implementation(project(":core-ai"))
    implementation(project(":chimera-core"))

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.mockito:mockito-core:5.8.0")
}

kapt {
    correctErrorTypes = true
}
