plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs")
}

android {
    namespace = "com.example.pawplan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pawplan"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        viewBinding = true
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
        freeCompilerArgs += "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" // Match your Compose version
    }
}

dependencies {
    // Core AndroidX libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Jetpack Compose dependencies
    implementation(libs.androidx.compose.ui)
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.compose.material:material-icons-extended:1.5.3")
    implementation(libs.volley)

    // Debugging tools for Compose
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:22.1.0")
    implementation("com.google.android.gms:play-services-auth:20.6.0")
    implementation("com.google.firebase:firebase-firestore:24.9.1")
    implementation("com.google.firebase:firebase-storage:20.2.1")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.6.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.6.0")
    implementation("androidx.navigation:navigation-compose:2.7.3")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.google.accompanist:accompanist-swiperefresh:0.28.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    implementation("com.squareup.picasso:picasso:2.71828")
}
