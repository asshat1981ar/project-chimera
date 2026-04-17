plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.chimera"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.chimera.ashes"
        minSdk = 24
        targetSdk = 34
        versionCode = (project.findProperty("VERSION_CODE") as? String)?.toIntOrNull() ?: 1
        versionName = (project.findProperty("VERSION_NAME") as? String) ?: "1.0.0-beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // AI provider API keys (set via local.properties or CI secrets)
        buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\"")
        buildConfigField("String", "GROQ_API_KEY", "\"${project.findProperty("GROQ_API_KEY") ?: ""}\"")
        buildConfigField("String", "OPENROUTER_API_KEY", "\"${project.findProperty("OPENROUTER_API_KEY") ?: ""}\"")
        buildConfigField("Boolean", "DEMO_MODE", "false")

        // Cloudflare cloud-save Worker (set via local.properties or CI secrets)
        // CHIMERA_CLOUD_SAVE_URL=https://chimera-saves.<account>.workers.dev
        // CHIMERA_CLOUD_SAVE_TOKEN=<secret — set via: wrangler secret put API_TOKEN>
        buildConfigField("String", "CLOUD_SAVE_URL",   "\"${project.findProperty("CHIMERA_CLOUD_SAVE_URL")   ?: ""}\"")
        buildConfigField("String", "CLOUD_SAVE_TOKEN", "\"${project.findProperty("CHIMERA_CLOUD_SAVE_TOKEN") ?: ""}\"")

        // HuggingFace — portrait generation via Inference API
        // HUGGING_FACE_TOKEN=hf_... (read/inference scope, set via local.properties or CI)
        buildConfigField("String", "HUGGING_FACE_TOKEN", "\"${project.findProperty("HUGGING_FACE_TOKEN") ?: ""}\"")
    }

    flavorDimensions += "environment"
    productFlavors {
        create("mock") {
            dimension = "environment"
            applicationIdSuffix = ".mock"
            buildConfigField("String", "PROVIDER_MODE", "\"FAKE\"")
            buildConfigField("String", "API_BASE_URL", "\"http://localhost:8080\"")
        }
        create("dev") {
            dimension = "environment"
            buildConfigField("String", "PROVIDER_MODE", "\"AUTO\"")
            buildConfigField("String", "API_BASE_URL", "\"https://api.chimera-rpg.dev\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "PROVIDER_MODE", "\"AUTO\"")
            buildConfigField("String", "API_BASE_URL", "\"https://api.chimera-rpg.app\"")
        }
    }

    signingConfigs {
        create("release") {
            val keystorePath = project.findProperty("KEYSTORE_PATH") as? String
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = project.findProperty("KEYSTORE_PASSWORD") as? String ?: ""
                keyAlias = project.findProperty("KEY_ALIAS") as? String ?: ""
                keyPassword = project.findProperty("KEY_PASSWORD") as? String ?: ""
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigningConfig = signingConfigs.findByName("release")
            if (releaseSigningConfig?.storeFile != null) {
                signingConfig = releaseSigningConfig
            }
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
        create("demo") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".demo"
            buildConfigField("Boolean", "DEMO_MODE", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
}

dependencies {
    // Project modules
    implementation(project(":chimera-core"))
    implementation(project(":core-model"))
    implementation(project(":core-ui"))
    implementation(project(":core-database"))
    implementation(project(":core-network"))
    implementation(project(":core-ai"))
    implementation(project(":core-data"))
    implementation(project(":domain"))
    implementation(project(":feature-home"))
    implementation(project(":feature-map"))
    implementation(project(":feature-dialogue"))
    implementation(project(":feature-camp"))
    implementation(project(":feature-journal"))
    implementation(project(":feature-party"))
    implementation(project(":feature-settings"))

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)

    // Navigation
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // Ktor HTTP Client (for AI providers)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.client.logging)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // JSON serialization
    implementation(libs.kotlinx.serialization.json)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // WorkManager + Hilt integration — NPC portrait sync, background tasks
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    kapt(libs.hilt.work.compiler)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)

    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}
