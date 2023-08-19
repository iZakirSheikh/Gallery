// TODO: Remove once KTIJ-19369 is fixed
@Suppress("DSL_SCOPE_VIOLATION") plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.firebase)
    alias(libs.plugins.hilt)
    alias(libs.plugins.crashanlytics)
    kotlin("kapt")
}
android {
    namespace = "com.prime.gallery"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.prime.gallery2"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "0.0.1-dev"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }
    buildTypes {
        // Make sure release is version is optimised.
        release {
            isMinifyEnabled = true
            isZipAlignEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        // Add necessary changes to debug apk.
        debug {
            // makes it possible to install both release and debug versions in same device.
            applicationIdSuffix = ".debug"
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
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn", "-Xcontext-receivers")
    }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.1" }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

// Not moving these to libs.version.toml because i think this is redundant.
dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    val compose_version = "1.6.0-alpha03"
    implementation("androidx.compose.ui:ui:$compose_version")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose_version")
    implementation("androidx.compose.animation:animation-graphics:$compose_version")
    // Integration with activities
    implementation("androidx.activity:activity-compose:1.7.2")
    // Material design icons
    implementation("androidx.compose.material:material-icons-core:$compose_version")
    implementation("androidx.compose.material:material-icons-extended:$compose_version")
    // The Accompanist Libraries
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("com.google.accompanist:accompanist-permissions:0.31.5-beta")
    //Lottie
    implementation("com.airbnb.android:lottie-compose:6.1.0")
    // Preferences and other widgets
    val toolkit_version = "1.0.10-beta"
    implementation("com.github.prime-zs.toolkit:preferences:$toolkit_version")
    implementation("com.github.prime-zs.toolkit:core-ktx:$toolkit_version")
    implementation("com.github.prime-zs.toolkit:material3:$toolkit_version")
    // Splash Screen API
    implementation("androidx.core:core-splashscreen:1.0.1")
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.2.2"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    // material3
    val material3 = "1.2.0-alpha05"
    implementation("androidx.compose.material3:material3:$material3")
    implementation("androidx.compose.material3:material3-window-size-class:$material3")
    // Google Play InAppUpdate
    val in_app_update_version = "2.1.0"
    implementation("com.google.android.play:app-update:$in_app_update_version")
    implementation("com.google.android.play:app-update-ktx:$in_app_update_version")
    // Google Play InAppReview
    val in_app_review = "2.0.1"
    implementation("com.google.android.play:review:$in_app_review")
    implementation("com.google.android.play:review-ktx:$in_app_review")
    // Google play in-app billing
    val billing_version = "6.0.1"
    implementation("com.android.billingclient:billing:$billing_version")
    implementation("com.android.billingclient:billing-ktx:$billing_version")
    // Unity Ads
    implementation("com.unity3d.ads:unity-ads:4.8.0")
    // Compose navigation
    implementation("androidx.navigation:navigation-compose:2.7.0")
    // Compose Downloadable fonts
    implementation("androidx.compose.ui:ui-text-google-fonts:1.5.0")
    // Hilt
    implementation("com.google.dagger:hilt-android:2.47")
    kapt("com.google.dagger:hilt-android-compiler:2.47")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
}