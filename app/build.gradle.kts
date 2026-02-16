import com.android.build.api.dsl.ApplicationDefaultConfig
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// -----------------------------------------------------------------------------
// PLUGINS
// Core build plugins for Android Application, Kotlin, Compose, and Firebase
// -----------------------------------------------------------------------------
plugins {
    alias(libs.plugins.android.application)      // Android Application plugin
    alias(libs.plugins.jetbrains.kotlin.android) // Kotlin Android plugin
    alias(libs.plugins.compose.compiler)         // Compose compiler plugin
    alias(libs.plugins.google.services)          // Google Services (Firebase integration)
    alias(libs.plugins.crashanlytics)            // Firebase Crashlytics
}

// -----------------------------------------------------------------------------
// BUILD CONFIG HELPERS
// Utility to add BuildConfig fields (e.g., secrets, constants)
// -----------------------------------------------------------------------------
/**
 * Adds a string BuildConfig field to the project.
 */
private fun ApplicationDefaultConfig.buildConfigField(name: String, value: String) =
    buildConfigField("String", name, "\"" + value + "\"")

/**
 * Secrets injected into BuildConfig at runtime (via GitHub env).
 */
val secrets = arrayOf(
    // "ADS_APP_ID", // Example placeholder
    "PLAY_CONSOLE_APP_RSA_KEY"
)

// -----------------------------------------------------------------------------
// KOTLIN COMPILER OPTIONS
// Configure JVM target and experimental compiler flags
// -----------------------------------------------------------------------------
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11 // Target JVM bytecode version

        freeCompilerArgs.addAll(
            // "-XXLanguage:+ExplicitBackingFields", // Explicit backing fields (disabled)
            "-XXLanguage:+NestedTypeAliases",       // Nested type aliases
            "-Xopt-in=kotlin.RequiresOptIn",        // Opt-in to @RequiresOptIn APIs
            "-Xwhen-guards",                        // Experimental when-guards
            "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi", // Compose foundation experimental
            "-Xopt-in=com.zs.compose.theme.ExperimentalThemeApi",             // Custom theme experimental
            "-Xnon-local-break-continue",           // Allow non-local break/continue
            "-Xcontext-sensitive-resolution",       // Context-sensitive overload resolution
            "-Xcontext-parameters"                  // Context parameters (experimental)
        )
    }
}

// -----------------------------------------------------------------------------
// ANDROID CONFIGURATION
// Namespace, SDK versions, build types, features, and packaging
// -----------------------------------------------------------------------------
android {
    namespace = "com.zs.gallery"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.googol.android.apps.photos"
        minSdk = 24
        targetSdk = 36
        versionCode = 71
        versionName = "0.9.1-dev"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // Load secrets into BuildConfig (from GitHub env)
        for (secret in secrets) {
            buildConfigField(secret, System.getenv(secret) ?: "")
        }
    }

    buildTypes {
        // Release build: optimized with shrinking + ProGuard
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        // Debug build: suffixes for coexistence with release
        debug {
            applicationIdSuffix = ".dev"
            resValue("string", "app_name", "Debug")
            versionNameSuffix = "-debug"
        }
    }

    // Java compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Enable Compose + BuildConfig features
    buildFeatures { compose = true; buildConfig = true }

    // Exclude redundant license files
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

// -----------------------------------------------------------------------------
// DEPENDENCIES
// Grouped by functional area: Core, Compose, Toolkit, UI, Firebase, Play Services
// -----------------------------------------------------------------------------
dependencies {
    // Local project modules
    implementation(project(":core"))

    // Compose core + BOM
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Navigation + Toolkit
    implementation(libs.navigation.compose)
    implementation(libs.toolkit.theme)
    implementation(libs.toolkit.foundation)
    implementation(libs.toolkit.preferences)

    // AndroidX utilities
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.startup)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.google.fonts)

    // Compose extensions
    implementation(libs.telephoto.zoomable)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.koin)
    implementation(libs.chrisbanes.haze)
    implementation(libs.lottie.compose)

    // Bundles
    implementation(libs.bundles.coil)   // Image loading
    implementation(libs.bundles.icons)  // Material icons

    // Firebase
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics.ktx)

    // Play Services
    implementation(libs.play.app.update.ktx)
    implementation(libs.play.app.review.ktx)

    // Debug-only tooling
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}