plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    id("kotlin-kapt")
}

android {
    namespace = "com.chimera.data"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":core-model"))
    implementation(project(":core-database"))

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Serialization (for SceneLoader, MapNodeLoader)
    implementation(libs.kotlinx.serialization.json)

    // DataStore (for ChimeraPreferences)
    implementation(libs.datastore.preferences)

    // Room (for entity types in repository signatures)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
}

kapt {
    correctErrorTypes = true
}
