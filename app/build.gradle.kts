import org.jetbrains.kotlin.gradle.dsl.JvmTarget


// -----------------------------------------------------------------------------
// PLUGINS
// -----------------------------------------------------------------------------
// 📦 Core plugins required for Android + Kotlin + Compose support.
plugins {
    alias(libs.plugins.android.application)   // Android application plugin
    alias(libs.plugins.kotlin.android)        // Kotlin support for Android
    alias(libs.plugins.kotlin.compose)        // Jetpack Compose UI toolkit
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
        // Target JVM bytecode version (was "11" string, now typed enum)
        jvmTarget = JvmTarget.JVM_17

        // Add experimental/advanced compiler flags
        freeCompilerArgs.addAll(
         //   "-XXLanguage:+ExplicitBackingFields", //  Explicit backing fields
            "-XXLanguage:+NestedTypeAliases",
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
// ANDROID CONFIGURATION
// -----------------------------------------------------------------------------
android {
    namespace = "com.zs.gallery"
    compileSdk { version = release(36) }
    buildFeatures { compose = true }
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
        applicationId = "com.googol.android.apps.photos"
        minSdk = 24
        targetSdk = 36
        versionCode = 1000
        versionName = "2.0.0-dev"
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
            versionNameSuffix = "-foss"
        }

        // PLUS (Privacy-friendly edition: ads + in-app purchases, but telemetry disabled)
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
        }
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
            applicationIdSuffix = ".dev"
            resValue("string", "launcher_label", "Debug")
            versionNameSuffix = "-debug" // 🔖 Adds "-debug" suffix to version name for clarity
        }
    }
}

// -----------------------------------------------------------------------------
// APP DEPENDENCIES
// -----------------------------------------------------------------------------
dependencies {
    // local
    implementation(project(":common"))
    //
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    //
    implementation(libs.bundles.nav3)
    implementation(libs.toolkit.theme)
    implementation(libs.toolkit.foundation)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.startup)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.google.fonts)
    implementation(libs.telephoto.zoomable)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.koin)
    implementation(libs.chrisbanes.haze)
    implementation(libs.lottie.compose)
    implementation(libs.bundles.coil)
    implementation(libs.androidx.paging.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}