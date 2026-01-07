plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.agritrack"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.agritrack"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Résolution de GEMINI_API_KEY - VERSION CORRIGÉE
        val geminiKeyFromProp = (project.findProperty("GEMINI_API_KEY") as String?)?.trim().orEmpty()
        val geminiKeyFromEnv = System.getenv("GEMINI_API_KEY")?.trim().orEmpty()
        val geminiKey: String = when {
            geminiKeyFromProp.isNotEmpty() -> geminiKeyFromProp
            geminiKeyFromEnv.isNotEmpty() -> geminiKeyFromEnv
            else -> {
                val lp = rootProject.file("local.properties")
                if (lp.exists()) {
                    try {
                        val lines = lp.readLines()
                        //noinspection WrongGradleMethod
                        lines.firstOrNull { line ->
                            line.trim().startsWith("GEMINI_API_KEY=")
                        }?.substringAfter("=")?.trim() ?: ""
                    } catch (e: Exception) {
                        ""
                    }
                } else {
                    ""
                }
            }
        }

        buildConfigField("String", "GEMINI_API_KEY", "\"${geminiKey.replace("\"", "\\\"")}\"")
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

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Pour éviter les warnings
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Désugaring pour les APIs Java 8+
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3")

    // AndroidX Core (depuis votre .toml)
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.core:core-ktx:1.13.1")

    // Material Design (depuis votre .toml)
    implementation(libs.material)

    // Tests (depuis votre .toml)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Room Database - VERSION ALTERNATIVE SI libs.room.runtime ne fonctionne pas
    val room_version = "2.6.0"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Cartes
    implementation("org.maplibre.gl:android-sdk:9.6.0")

    // Services Google
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-safetynet:18.1.0")

    // CameraX
    val camerax_version = "1.2.3"
    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:1.2.3")

    // Utilitaires
    implementation("com.google.guava:guava:31.1-android")
    implementation("com.google.mlkit:barcode-scanning:17.0.2")

    // Graphiques
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // PDF
    implementation("com.itextpdf:itextpdf:5.5.13.3")
    implementation("com.itextpdf:kernel:7.2.5")
    implementation("com.itextpdf:layout:7.2.5")

    // Réseau
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Asynchrone
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Si vous utilisez ViewModel/LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.biometric:biometric:1.1.0")
}