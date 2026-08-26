// -----------------------------------------------------------------------------
// PLUGIN MANAGEMENT
// -----------------------------------------------------------------------------
// 🛠️ Configures where Gradle looks for build-time plugins.
// Includes regex filtering for Google and AndroidX artifacts to speed up
// resolution and prevent dependency confusion attacks.
// -----------------------------------------------------------------------------
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

// -----------------------------------------------------------------------------
// BUILD CONVENTIONS & TOOLCHAINS
// -----------------------------------------------------------------------------
// ⚙️ Automates JDK management. The Foojay resolver ensures the correct
// Java version is downloaded and configured automatically for the project.
// -----------------------------------------------------------------------------
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// -----------------------------------------------------------------------------
// DEPENDENCY RESOLUTION
// -----------------------------------------------------------------------------
// 📦 Global repository settings for project dependencies.
// FAIL_ON_PROJECT_REPOS forces all modules to use these centralized settings.
// -----------------------------------------------------------------------------
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

// -----------------------------------------------------------------------------
// PROJECT STRUCTURE
// -----------------------------------------------------------------------------
// 📂 Defines the project name and the list of modules to be included in the
// multi-module build.
// -----------------------------------------------------------------------------
rootProject.name = "Gallery"
include(":app")
include(":domain")