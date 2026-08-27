import com.android.build.api.dsl.AndroidLibrarySourceSet
import com.android.build.api.dsl.LibraryProductFlavor
import com.android.build.api.dsl.VariantDimension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.android.build.api.dsl.LibraryDefaultConfig as Config

/**
 * 🛠 HELPER: Adds a String field to BuildConfig.
 * Wraps the value in quotes to ensure it is treated as a String literal in Java/Kotlin.
 */
private operator fun VariantDimension.set(name: String, value: String) =
    buildConfigField("String", name, "\"" + value + "\"")

// -----------------------------------------------------------------------------
// SECRETS
// -----------------------------------------------------------------------------
// 🔐 Keys or IDs injected into BuildConfig at runtime.
// These are pulled from the local environment or CI/CD secrets.
private val secrets = arrayOf(/*"ADS_APP_ID",*/ "PLAY_CONSOLE_APP_RSA_KEY")

// -----------------------------------------------------------------------------
// PLUGINS
// -----------------------------------------------------------------------------
plugins {
    alias(libs.plugins.android.library)          // Android Library plugin
    alias(libs.plugins.ksp)
}

// ⚙️ DEFAULT CONFIGURATION
// Defined as a lambda to be applied within the 'android' block.
private val config: Config.() -> Unit = {
    minSdk = 24
    // 📌 Edition constants (used for comparison in code)
    this["FLAVOR_COMMUNITY"] = "community"
    this["FLAVOR_STANDARD"] = "standard"
    this["FLAVOR_PLUS"] = "plus"
    this["FLAVOR_GOLD"] = "gold"
    // Inject secrets from environment variables.
    // Missing values default to empty strings to avoid build failures.
    for (secret in secrets)
        this[secret] = System.getenv(secret) ?: ""
}

//🧊 PRODUCT FLAVORS
//Configuration for different versions (e.g., Free vs Pro).
private val flavours: NamedDomainObjectContainer<LibraryProductFlavor>.() -> Unit = {
    // STANDARD → Default monetized edition.
    // PLUS + Ad SDK
    create("standard") { dimension = "edition" }

    // COMMUNITY → FOSS/open‑source build.
    // Minimal free edition with no ads, no telemetry, and no purchases.
    create("community") { dimension = "edition" }

    // PLUS → Privacy-friendly edition:
    // No Ad SDK, but telemetry and in‑app purchases.
    create("plus") { dimension = "edition" }

    // PREMIUM → Full unlock build.
    // Based on Community, but with all features enabled.
    create("gold") { dimension = "edition" }
}

// 📂 SOURCE SETS
// Customizes the directory structure for source files, resources, and manifests.
// Useful for flavor-specific logic or test directories.
private val sources: NamedDomainObjectContainer<AndroidLibrarySourceSet>.() -> Unit  = {
// Community flavor → uses stubbed (no-op) implementations for all shared libs
    getByName("community") {
        kotlin.directories += "src/shared/analytics/stub/java"
        kotlin.directories += "src/shared/ads/stub/java"
        kotlin.directories += "src/shared/billing/stub/java"
    }

    // Premium flavor → also wired to stub implementations (restricted feature set)
    getByName("plus") {
        kotlin.directories += "src/shared/analytics/actual/java"
        kotlin.directories += "src/shared/ads/stub/java"
        kotlin.directories += "src/shared/billing/actual/java"
    }

    // Standard flavor → full/actual implementations of analytics, billing, and ads
    getByName("standard") {
        kotlin.directories += "src/shared/analytics/actual/java"
        kotlin.directories += "src/shared/ads/actual/java"
        kotlin.directories += "src/shared/billing/actual/java"
    }

    // Gold flavor → only requires actual market implementation (no analytics/ads)
    getByName("gold") {
        kotlin.directories += "src/shared/analytics/stub/java"
        kotlin.directories += "src/shared/ads/stub/java"
        kotlin.directories += "src/shared/billing/stub/java"
    }
}

// -----------------------------------------------------------------------------
// KOTLIN COMPILER OPTIONS
// -----------------------------------------------------------------------------
kotlin {
    compilerOptions {
        // Target JVM bytecode version (typed enum instead of raw string)
        jvmTarget = JvmTarget.JVM_11

        // Advanced / experimental compiler flags
        freeCompilerArgs.addAll(
            "-Xopt-in=kotlin.RequiresOptIn",        // Opt-in to @RequiresOptIn APIs
        )
    }
}

// ============================================================================
// ANDROID CONFIGURATION
// ============================================================================
android {
    namespace = "com.zs.domain"
    compileSdk { version = release(37) }
    buildFeatures { buildConfig = true }

    // Apply our pre-defined configurations
    defaultConfig(config)
    flavorDimensions += "edition"
    productFlavors(flavours)
    sourceSets(sources)

    // Java toolchain compatibility (matching the Kotlin JVM target)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    implementation(libs.room.paging)
    implementation(libs.androidx.paging.runtime)
    // Plus only
    "plusImplementation"(libs.bundles.play.services)
    // Standard only
    "standardImplementation"(libs.bundles.play.services)
    "standardImplementation"(libs.bundles.analytics)
    "standardImplementation"(libs.bundles.ads)
}