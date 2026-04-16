pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Project Chimera"
include(":app")
include(":chimera-core")
include(":core-model")
include(":core-ui")
include(":core-database")
include(":core-network")
include(":core-ai")
include(":core-data")
include(":domain")
include(":feature-home")
include(":feature-map")
include(":feature-dialogue")
include(":feature-camp")
include(":feature-journal")
include(":feature-party")
include(":feature-settings")
