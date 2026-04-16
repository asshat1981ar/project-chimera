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
include(":core-model")
include(":core-ui")
include(":core-database")
include(":core-network")
include(":core-ai")
include(":core-data")
include(":domain")
