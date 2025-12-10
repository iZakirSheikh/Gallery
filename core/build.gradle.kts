plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=com.primex.core.ExperimentalToolkitApi",
            "-Xwhen-guards",
            "-Xnon-local-break-continue"
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation (libs.androidx.exifinterface)
    api(libs.bundles.coil)
    implementation(libs.androidx.activity.compose)
    implementation(libs.bundles.media3)
    implementation(libs.google.billing.ktx)
}