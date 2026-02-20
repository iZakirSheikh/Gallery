import com.android.build.api.dsl.VariantDimension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// -----------------------------------------------------------------------------
// SECRETS
// -----------------------------------------------------------------------------
// 🔐 Keys or IDs injected into BuildConfig at runtime.
private val secrets = arrayOf(/*"ADS_APP_ID",*/ "PLAY_CONSOLE_APP_RSA_KEY")
// -----------------------------------------------------------------------------
// PLUGINS
// -----------------------------------------------------------------------------
plugins {
    alias(libs.plugins.android.library)          // Android Library plugin
    alias(libs.plugins.jetbrains.kotlin.android) // Kotlin Android plugin
}

/** Adds a string BuildConfig field to the project. */
private fun VariantDimension.buildConfigField(name: String, value: String) =
    buildConfigField("String", name, "\"" + value + "\"")

// -----------------------------------------------------------------------------
// KOTLIN COMPILER OPTIONS
// -----------------------------------------------------------------------------
kotlin {
    compilerOptions {
        // Target JVM bytecode version (typed enum instead of raw string)
        jvmTarget = JvmTarget.JVM_11

        // Advanced / experimental compiler flags
        freeCompilerArgs.addAll(
            // "-XXLanguage:+ExplicitBackingFields", // Explicit backing fields (disabled for now)
            "-XXLanguage:+NestedTypeAliases",       // Nested type aliases
            "-Xopt-in=kotlin.RequiresOptIn",        // Opt-in to @RequiresOptIn APIs
            "-Xwhen-guards",                        // Experimental when-guards
            "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi", // Compose foundation experimental
            "-Xnon-local-break-continue",           // Allow non-local break/continue
            "-Xcontext-sensitive-resolution",       // Context-sensitive overload resolution
            "-Xcontext-parameters"                  // Context parameters (experimental)
        )
    }
}

// ============================================================================
// ANDROID CONFIGURATION
// ============================================================================
android {
    namespace = "com.zs.core"
    compileSdk { version = release(36) }
    buildFeatures { buildConfig = true }

    // Java compatibility settings
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
        buildConfigField("FLAVOR_COMMUNITY", "community")
        buildConfigField("FLAVOR_STANDARD", "standard")
        buildConfigField("FLAVOR_PLUS", "plus")
        buildConfigField("FLAVOR_PREMIUM", "premium")
    }
    // -------------------------------------------------------------------------
    // PRODUCT FLAVORS
    // -------------------------------------------------------------------------
    flavorDimensions += "edition"
    productFlavors {
        // STANDARD → Default monetized edition.
        // PLUS + Ad SDK
        create("standard") { dimension = "edition" }

        // COMMUNITY → FOSS/open‑source build.
        // Minimal free edition with no ads, no telemetry, and no purchases.
        create("community") { dimension = "edition" }

        // PLUS → Privacy-friendly edition:
        // No Ad SDK, but telemetry and in‑app purchases.
        //create("plus") { dimension = "edition" }

        // PREMIUM → Full unlock build.
        // Based on Community, but with all features enabled.
        //create("premium") { dimension = "edition" }
    }
    // -------------------------------------------------------------------------
    // SOURCE SETS CONFIGURATION
    // -------------------------------------------------------------------------
    sourceSets {
        // Community flavor → uses stubbed (no-op) implementations for all shared libs
        getByName("community") {
            java.srcDirs(
                "src/shared/analytics/stub/java",
                "src/shared/billing/stub/java",
                "src/shared/ads/stub/java",
                "src/shared/market/stub/java"
            )
        }

        // Premium flavor → also wired to stub implementations (restricted feature set)
     /*   getByName("premium") {
            java.srcDirs(
                "src/shared/analytics/stub/java",
                "src/shared/billing/stub/java",
                "src/shared/ads/stub/java"
            )
        }*/

        // Standard flavor → full/actual implementations of analytics, billing, and ads
        getByName("standard") {
            java.srcDirs(
                "src/shared/analytics/actual/java",
                "src/shared/billing/actual/java",
                "src/shared/ads/actual/java",
                "src/shared/market/actual/java"
            )
        }

        /*// Plus flavor → only requires actual billing implementation (no analytics/ads)
        getByName("plus") {
            java.srcDirs("src/shared/billing/actual/java")
        }*/
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
    implementation(libs.androidx.exifinterface)
    api(libs.bundles.coil)
    implementation(libs.androidx.activity.compose)
    implementation(libs.bundles.media3)

    // Standard only
    //"standardImplementation"(libs.bundles.play.services)
    "standardImplementation"(libs.bundles.analytics)
    "standardImplementation"(libs.google.billing.ktx)
    // Play Services
    "standardImplementation"(libs.play.app.update.ktx)
    "standardImplementation"(libs.play.app.review.ktx)
   // "standardImplementation"(libs.bundles.ads)
}