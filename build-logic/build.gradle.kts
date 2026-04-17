plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("chimeraAndroidLibrary") {
            id = "chimera.android.library"
            implementationClass = "ChimeraAndroidLibraryPlugin"
        }
        register("chimeraAndroidLibraryCompose") {
            id = "chimera.android.library.compose"
            implementationClass = "ChimeraAndroidLibraryComposePlugin"
        }
    }
}
