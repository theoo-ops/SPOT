plugins {
    alias(libs.plugins.android.application)
    // AGP 9 compiles Kotlin itself; only the Compose compiler plugin is added.
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.spot"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.spot"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                // No packageScope: R8's partial mode (optimize only androidx/kotlin/kotlinx)
                // moved Kotlin stdlib classes away from a package-private parent and crashed
                // on launch with IllegalAccessError. Whole-app R8 is the well-tested path.
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.navigation.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
