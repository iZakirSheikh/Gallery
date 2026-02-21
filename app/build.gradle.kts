import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// -----------------------------------------------------------------------------
// PLUGINS
// -----------------------------------------------------------------------------
// 📦 Core plugins required for Android + Kotlin + Compose support.
plugins {
    alias(libs.plugins.android.application)   // Android application plugin
    alias(libs.plugins.jetbrains.kotlin.android) // Kotlin Android plugin
    alias(libs.plugins.compose.compiler)         // Compose compiler plugin
    // TODO - Find a way to apply these to only standard flavour
    // ⚠️ Currently Crashlytics + Google Services are applied globally.
    alias(libs.plugins.crashanlytics) // Firebase Crashlytics (should be flavor-scoped)
    alias(libs.plugins.google.services) // Google Services (should be flavor-scoped)
}
// -----------------------------------------------------------------------------
// KOTLIN COMPILER OPTIONS
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
// ANDROID CONFIGURATION
// -----------------------------------------------------------------------------
android {
    namespace = "com.zs.gallery"
    compileSdk { version = release(36) }
    buildFeatures { compose = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } } // Exclude redundant license files

    //
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // -----------------------------------------------------------------------------
    // DEFAULT CONFIGURATION
    // -----------------------------------------------------------------------------
    // 📦 Core app settings: ID, SDK versions, versioning, and test runner.
    defaultConfig {
        applicationId = "com.googol.android.apps.photos"
        minSdk = 24
        targetSdk = 36
        versionCode = 77
        versionName = "1.0.4"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    // -------------------------------------------------------------------------
    // PRODUCT FLAVORS
    // -------------------------------------------------------------------------
    flavorDimensions += "edition"
    productFlavors {
        // STANDARD (Default monetized edition: ads + telemetry + in-app purchases enabled)
        create("standard") { dimension = "edition" }

        // COMMUNITY (Open-source edition: minimal free build, no ads, no telemetry, no purchases)
        create("community") {
            dimension = "edition"
            applicationId = "com.zs.android.apps.photos"
            versionNameSuffix = "-foss"
        }

/*        // PLUS (Privacy-friendly edition: ads + in-app purchases, but telemetry disabled)
        create("plus") {
            dimension = "edition"
            versionNameSuffix = "-plus"
            applicationIdSuffix = ".plus"
        }

        // PREMIUM (Full unlock edition: all features enabled, no ads, no telemetry, no purchases)
        create("premium") {
            dimension = "edition"
            versionNameSuffix = "-pro"
            applicationIdSuffix = ".pro"
        }*/
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
            // 📛 Appends ".debug" to the application ID so debug and release can coexist
            // applicationIdSuffix = ".dev"
            resValue("string", "launcher_label", "Debug")
            versionNameSuffix = "-debug" // 🔖 Adds "-debug" suffix to version name for clarity
        }
    }
}

// -----------------------------------------------------------------------------
// APP DEPENDENCIES
// -----------------------------------------------------------------------------
dependencies {
    // Local project modules
    implementation(project(":common"))

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

    // Debug-only tooling
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}