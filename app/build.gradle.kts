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

        // 🔥 AJOUTER CETTE LIGNE POUR CORRIGER L'ERREUR 16KB
        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
            abiFilters.add("x86")
            abiFilters.add("x86_64")
        }
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

    // 🔥 AJOUTER CETTE SECTION POUR LE PACKAGING
    packaging {
        resources {
            excludes += setOf(
                "META-INF/*.kotlin_module",
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/DEPENDENCIES"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // 🔥 AJOUTER SI VOUS UTILISEZ VIEW BINDING OU DATA BINDING
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // RecyclerView et CardView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

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