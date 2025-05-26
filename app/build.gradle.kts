import com.android.build.api.dsl.ApplicationDefaultConfig

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.service)
    alias(libs.plugins.crashanlytics)
}

/**
 * Adds a string BuildConfig field to the project.
 */
private fun ApplicationDefaultConfig.buildConfigField(name: String, value: String) =
    buildConfigField("String", name, "\"" + value + "\"")

/**
 * The secrets that needs to be added to BuildConfig at runtime.
 */
val secrets = arrayOf(
//    "ADS_APP_ID",
    "PLAY_CONSOLE_APP_RSA_KEY",
)

android {
    namespace = "com.zs.gallery"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.googol.android.apps.photos"
        minSdk = 24
        targetSdk = 35
        versionCode = 62
        versionName = "0.7.3-dev"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
        // Load secrets into BuildConfig
        // These are passed through env of github.
        for (secret in secrets) {
            buildConfigField(secret, System.getenv(secret) ?: "")
        }
    }
    buildTypes {
        // Make sure release is version is optimised.
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }

        // Add necessary changes to debug apk.
        debug {
            // makes it possible to install both release and debug versions in same device.
            applicationIdSuffix = ".dev"
            resValue("string", "app_name", "Debug")
            versionNameSuffix = "-debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-Xopt-in=com.zs.compose.theme.ExperimentalThemeApi",
            "-Xwhen-guards",
            "-Xnon-local-break-continue"
        )
    }
    buildFeatures { compose = true; buildConfig = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    implementation(libs.androidx.koin)
    implementation(libs.toolkit.preferences)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.accompanist.permissions)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.saket.zoomable)
    implementation(libs.chrisbanes.haze)
    implementation(libs.play.app.update.ktx)
    implementation(libs.play.app.review.ktx)

    // ui
    implementation(libs.androidx.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.lottie.compose)
    implementation(libs.toolkit.theme)
    implementation(libs.toolkit.foundation)

    // local
    implementation(project(":core"))

    // bundles
    implementation(libs.bundles.icons)
    implementation(libs.bundles.compose.ui)

    implementation(libs.bundles.compose.ui.tooling)
    //implementation("dev.chrisbanes.haze:haze-materials:1.5.3")
}