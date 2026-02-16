import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// -----------------------------------------------------------------------------
// PLUGINS
// Core build plugins for Android + Kotlin support
// -----------------------------------------------------------------------------
plugins {
    alias(libs.plugins.android.library)          // Android Library plugin
    alias(libs.plugins.jetbrains.kotlin.android) // Kotlin Android plugin
}

// -----------------------------------------------------------------------------
// KOTLIN COMPILER OPTIONS
// Configure Kotlin compiler behavior, JVM target, and experimental flags
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

// -----------------------------------------------------------------------------
// ANDROID CONFIGURATION
// Namespace, SDK versions, build types, and Java compatibility
// -----------------------------------------------------------------------------
android {
    namespace = "com.zs.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Java compatibility settings
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// -----------------------------------------------------------------------------
// DEPENDENCIES
// -----------------------------------------------------------------------------
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.exifinterface)
    api(libs.bundles.coil)
    implementation(libs.androidx.activity.compose)
    implementation(libs.bundles.media3)
    implementation(libs.google.billing.ktx)
}
