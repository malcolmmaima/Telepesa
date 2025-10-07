pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "Telepesa"
include(":app")

// Core modules
include(":core:common")
include(":core:domain")
include(":core:data")
include(":core:ui")
include(":core:network")
include(":core:database")
include(":core:security")

// Feature modules (Dynamic Feature Modules)
include(":feature:onboarding")
include(":feature:auth")
include(":feature:home")