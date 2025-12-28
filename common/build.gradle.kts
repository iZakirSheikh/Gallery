
import com.android.build.api.dsl.VariantDimension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// -----------------------------------------------------------------------------
// SECRETS
// -----------------------------------------------------------------------------
// 🔐 Keys or IDs injected into BuildConfig at runtime.
private val secrets = arrayOf("ADS_APP_ID", "PLAY_CONSOLE_APP_RSA_KEY")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

/** Adds a string BuildConfig field to the project. */
private fun VariantDimension.buildConfigField(name: String, value: String) =
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
            "-Xexplicit-backing-fields", //  Explicit backing fields
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

// ============================================================================
// ANDROID CONFIGURATION
// ============================================================================
android {
    namespace = "com.zs.common"
    compileSdk { version = release(36) }
    buildFeatures { buildConfig = true }

    // JAVA Config
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // DEFAULT CONFIGURATION
    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // --------------------------------------------------------------------
        // BUILD CONFIG: SECRETS
        // --------------------------------------------------------------------
        // Inject secrets from environment variables.
        // Missing values default to empty strings to avoid build failures.
        for (secret in secrets)
            buildConfigField(secret, System.getenv(secret) ?: "")

        // 📌 Edition constants (used for comparison in code)
        buildConfigField("FLAVOR_COMMUNITY", "_community")
        buildConfigField("FLAVOR_STANDARD", "_standard")
        buildConfigField("FLAVOR_PLUS", "_plus")
        buildConfigField("FLAVOR_PREMIUM", "_premium")
    }

    // -------------------------------------------------------------------------
    // PRODUCT FLAVORS
    // -------------------------------------------------------------------------
    flavorDimensions += "edition"
    productFlavors {
        // STANDARD (Default monetized edition: ads + telemetry + in-app purchases enabled)
        create("standard") { dimension = "edition" }

        // COMMUNITY (Open-source edition: minimal free build, no ads, no telemetry, no purchases)
        create("community") { dimension = "edition" }

        // PLUS (Privacy-friendly edition: ads + in-app purchases, but telemetry disabled)
        create("plus") { dimension = "edition" }

        // PREMIUM (Full unlock edition: all features enabled, no ads, no telemetry, no purchases)
        create("premium") { dimension = "edition" }
    }

    // BUILD TYPES
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

// ============================================================================
// DEPENDENCIES
// ============================================================================
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.work.runtime.ktx)
    ksp(libs.room.compiler)
    implementation(libs.coil.core)
    api(libs.toolkit.preferences)
    // Plus includes only
    "plusImplementation"(libs.google.billing.ktx)
    "plusImplementation"(libs.play.app.update.ktx)
    "plusImplementation"(libs.play.app.review.ktx)
    /*For standard as well*/
    "standardImplementation"(libs.google.billing.ktx)
    "standardImplementation"(libs.play.app.update.ktx)
    "standardImplementation"(libs.play.app.review.ktx)
    "standardImplementation"(libs.firebase.analytics.ktx)
    "standardImplementation"(libs.firebase.crashlytics.ktx)
    "standardImplementation"(libs.unity.ads.mediation)
    "standardImplementation"(libs.unity.ads.adquality)
}