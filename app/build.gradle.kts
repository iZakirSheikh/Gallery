import com.android.build.api.dsl.ApplicationBaseFlavor
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// -----------------------------------------------------------------------------
// SECRETS
// -----------------------------------------------------------------------------
// 🔐 Keys or IDs injected into BuildConfig at runtime.
private val secrets = arrayOf("ADS_APP_ID", "PLAY_CONSOLE_APP_RSA_KEY")

// -----------------------------------------------------------------------------
// PLUGINS
// -----------------------------------------------------------------------------
// 📦 Core plugins required for Android + Kotlin + Compose support.
plugins {
    alias(libs.plugins.android.application)   // Android application plugin
    alias(libs.plugins.kotlin.android)        // Kotlin support for Android
    alias(libs.plugins.kotlin.compose)        // Jetpack Compose UI toolkit
}

/** Adds a string BuildConfig field to the project. */
private fun ApplicationBaseFlavor.buildConfigField(name: String, value: String) =
    buildConfigField("String", name, "\"" + value + "\"")

// -----------------------------------------------------------------------------
// KOTLIN COMPILER OPTIONS
// -----------------------------------------------------------------------------
kotlin {
    compilerOptions {
        // Target JVM bytecode version (was "11" string, now typed enum)
        jvmTarget = JvmTarget.JVM_17

        // Add experimental/advanced compiler flags
        freeCompilerArgs.addAll(
            "-Xopt-in=kotlin.RequiresOptIn", // Opt-in to @RequiresOptIn APIs
            "-Xwhen-guards",                 // Enable experimental when-guards
            "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi", // Compose foundation experimental
            "-Xopt-in=com.zs.compose.theme.ExperimentalThemeApi",             // Custom theme experimental
            "-Xnon-local-break-continue",    // Allow non-local break/continue
            "-Xcontext-sensitive-resolution",// Context-sensitive overload resolution
            "-Xcontext-parameters"           // Enable context parameters (experimental)
        )
    }
}

// -----------------------------------------------------------------------------
// APP DEPENDENCIES
// -----------------------------------------------------------------------------
dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    //
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// -----------------------------------------------------------------------------
// ANDROID CONFIGURATION
// -----------------------------------------------------------------------------
android {
    namespace = "com.zs.gallery"
    compileSdk { version = release(36) }
    buildFeatures { compose = true; buildConfig = true }
    //
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    // -----------------------------------------------------------------------------
    // COMPOSE COMPILER CONFIGURATION
    // -----------------------------------------------------------------------------
    // ⚙️ Controls advanced Compose compiler reporting and stability checks.
    // Reports/metrics can be enabled for debugging but are usually disabled in release builds.
    composeCompiler {
        // TODO - I guess disable these in release builds.reportsDestination =
        // layout.buildDirectory.dir("compose_compiler")
        // metricsDestination = layout.buildDirectory.dir("compose_compiler")
        stabilityConfigurationFiles = listOf(
            rootProject.layout.projectDirectory.file("stability_config.conf")
        )
    }
    // -----------------------------------------------------------------------------
    // DEFAULT CONFIGURATION
    // -----------------------------------------------------------------------------
    // 📦 Core app settings: ID, SDK versions, versioning, and test runner.
    defaultConfig {
        applicationId =  "com.googol.android.apps.oneplayer"
        minSdk = 24
        targetSdk = 36
        versionCode = 100
        versionName = "1.6.0-dev"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 🔐 Load secrets into BuildConfig (from GitHub Actions env variables)
        for (secret in secrets) {
            buildConfigField(secret, System.getenv(secret) ?: "")
        }
        // default edition is freemium
        buildConfigField( "EDITION", "freemium")
        // 📌 Edition constants (used for comparison in code)
        buildConfigField("EDITION_FOSS", "foss")
        buildConfigField("EDITION_FREEMIUM", "freemium")
        buildConfigField("EDITION_PRO", "pro")
    }
    // -----------------------------------------------------------------------------
    // Build Types
    // -----------------------------------------------------------------------------
    buildTypes {
        // -------------------------------------------------------------------------
        // RELEASE BUILD
        // -------------------------------------------------------------------------
        release {
            // ⚙️ Code shrinking/obfuscation (ProGuard/R8) and resource shrinking
            isMinifyEnabled = true          // Enable code shrinking/obfuscation
            isShrinkResources = true        // Remove unused resources to reduce APK size

            // 📜 ProGuard/R8 rules for release builds
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // 🔑 Signing configuration (currently using debug keys for convenience)
            // signingConfig = signingConfigs.getByName("debug")
        }

        // -------------------------------------------------------------------------
        // DEBUG BUILD
        // -------------------------------------------------------------------------
        debug {
            applicationIdSuffix = ".debug"  // 📛 Appends ".debug" to the application ID so debug and release can coexist
            resValue("string", "launcher_label", "Debug")  // 🏷️ Custom string resource for launcher label in debug builds
            versionNameSuffix = "-debug" // 🔖 Adds "-debug" suffix to version name for clarity
        }
    }
    // -------------------------------------------------------------------------
    // PRODUCT FLAVORS
    // -------------------------------------------------------------------------
    flavorDimensions += "edition"
    productFlavors {
        /// FREEMIUM (Default flavor, ads + purchases enabled)
        create("freemium") { dimension = "edition" }

        // PRO (All features unlocked, no ads/telemetry)
        create("pro") {
            dimension = "edition"
            versionNameSuffix = "-pro"
            // Override edition field for this flavor
            buildConfigField("EDITION", "pro")
        }

        // FOSS (Minimal free edition, no ads/telemetry)
        create("foss") {
            dimension = "edition"
            versionNameSuffix = "-foss"
            // Override edition field for this flavor
            buildConfigField("EDITION", "foss")
        }
    }
}