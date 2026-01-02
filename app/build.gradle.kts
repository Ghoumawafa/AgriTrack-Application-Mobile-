plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.agritrack"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.agritrack"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.android.material:material:1.11.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Room Database
    val room_version = "2.6.0"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
// GSON pour la sérialisation
    implementation("com.google.code.gson:gson:2.10.1")

    // Map (MapLibre) for terrain location selection
    implementation("org.maplibre.gl:android-sdk:9.6.0")

    // GPS / fused location provider for automatic terrain positioning
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // CameraX and ML Kit Barcode scanning
    val camerax_version = "1.2.3"
    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:1.2.3")

    // ML Kit Barcode Scanning (on-device)
    implementation("com.google.mlkit:barcode-scanning:17.0.2")
}