import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.detekt)
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(rootProject.file("detekt.yml"))
        buildUponDefaultConfig = true
        parallel = true
        // Note: the detekt 1.23.x plugin does NOT wire extension.baseline into the
        // Detekt task's baseline input, so setting it here alone has no effect on
        // `detekt` runs. We set the baseline on the task directly below instead.
        // This is kept only so `detektBaseline` (the generator task) reads it.
        baseline = project.file("detekt-baseline.xml")
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "1.8"
        // Set the baseline directly on the task — per-subproject resolution so
        // each module's own detekt-baseline.xml is applied (not a single root file).
        val baselineFile = project.file("detekt-baseline.xml")
        if (baselineFile.exists()) {
            baseline.set(baselineFile)
        }
        reports {
            html.required.set(true)
            txt.required.set(false)
            sarif.required.set(false)
        }
    }
}
