plugins {
    id("chimera.android.library.compose")
}

android {
    namespace = "com.chimera.ui"
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.ui.tooling.preview)

    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.coil.compose)

    testImplementation(libs.junit)
}
