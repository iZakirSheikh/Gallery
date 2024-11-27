plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.service)
    alias(libs.plugins.crashanlytics)
}

android {
    namespace = "com.zs.gallery"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.googol.android.apps.photos"
        minSdk = 24
        targetSdk = 35
        versionCode = 32
        versionName = "0.2.1-dev"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xcontext-receivers",
            "-Xopt-in=com.primex.core.ExperimentalToolkitApi"
        )
    }
    buildFeatures { compose = true; buildConfig = true  }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.navigation.compose)
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


    // bundles
    implementation(libs.bundles.play.services)
    implementation(libs.bundles.material.icons)

    // local
    implementation(project(":domain"))
    implementation(project(":foundation"))
}