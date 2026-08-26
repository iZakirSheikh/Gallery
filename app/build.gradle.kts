import com.android.build.api.dsl.ApplicationProductFlavor
import com.android.build.api.dsl.ApplicationDefaultConfig as Config

// -----------------------------------------------------------------------------
// CONFIGURATION PROPERTIES
// -----------------------------------------------------------------------------
// 🛠️ Base application parameters such as versioning and SDK targets.
private val config: Config.() -> Unit  = {
    versionCode = 1000
    versionName = "2.0.0-dev"
    minSdk = 28
    targetSdk = 37
    applicationId = "com.zs.android.apps.photos"
}

// -----------------------------------------------------------------------------
// PLUGINS
// -----------------------------------------------------------------------------
// 📦 Core plugins required for Android + Kotlin + Compose support.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
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
        rootProject.layout.projectDirectory.file("stability.config")
    )
}

// -----------------------------------------------------------------------------
// PRODUCT FLAVORS
// -----------------------------------------------------------------------------
// 🎨 Define different versions of the app (e.g., free vs pro).
private val flavors: NamedDomainObjectContainer<ApplicationProductFlavor>.() -> Unit  = {
    // STANDARD (Default monetized edition: ads + telemetry + in-app purchases enabled)
    create("standard") { dimension = "edition"; applicationId = "com.googol.android.apps.photos" }

    // COMMUNITY (Open-source edition: minimal free build, no ads, no telemetry, no purchases)
    create("community") {
        dimension = "edition"
        versionNameSuffix = "-foss"
    }

    // PLUS (Privacy-friendly edition: ads + in-app purchases, but telemetry disabled)
    create("plus") {
        dimension = "edition"
        versionNameSuffix = "-plus"
    }

    // PREMIUM (Full unlock edition: all features enabled, no ads, no telemetry, no purchases)
    create("premium") {
        dimension = "edition"
        versionNameSuffix = "-pro"
        applicationIdSuffix = ".pro"
    }
}

// -----------------------------------------------------------------------------
// ANDROID BLOCK
// -----------------------------------------------------------------------------
// 🤖 Primary Android-specific build settings and toolchain configuration.
android {
    // 🆔 Application Identity and SDK targets.
    namespace = "com.zs.gallery"
    compileSdk { version = release(37) }
    defaultConfig(action = config)
    buildFeatures { compose = true }

    // 🎨 Variant management and flavor definitions.
    flavorDimensions += "edition"
    productFlavors(flavors)

    // 📦 Resource and library packaging rules.
    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }  // Exclude redundant license files
        jniLibs.keepDebugSymbols.add("**/*.so")
    }

    // ☕ Java version compatibility for the compiler.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // 🚀 Build variants and optimization settings.
    buildTypes {
        // -------------------------------------------------------------------------
        // RELEASE BUILD
        // -------------------------------------------------------------------------
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            optimization { enable = true }
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
// DEPENDENCIES
// -----------------------------------------------------------------------------
// 📚 External libraries and frameworks required to build and run the app.
dependencies {
    // local
    implementation(project(":domain"))
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