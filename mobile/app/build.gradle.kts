plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.agripredict"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.agripredict"
        minSdk = 26 // Requis par les icônes adaptives + ~90% des appareils
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Support des langues : français, anglais, hausa, zarma
    androidResources {
        localeFilters += listOf("fr", "en", "ha", "dje")
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    // === AndroidX Core ===
    implementation(libs.androidx.core.ktx)

    // === Lifecycle (ViewModel, Flow, Compose) ===
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // === Jetpack Compose ===
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // === Navigation Compose ===
    implementation(libs.androidx.navigation.compose)

    // === Room (base de données locale) ===
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // === Retrofit + OkHttp (API REST) ===
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // === Kotlinx Serialization ===
    implementation(libs.kotlinx.serialization.json)

    // === Coroutines ===
    implementation(libs.kotlinx.coroutines.android)

    // === WorkManager (synchronisation en arrière-plan) ===
    implementation(libs.androidx.work.runtime.ktx)

    // === TensorFlow Lite (inférence IA) ===
    implementation(libs.tensorflow.lite)
    // tensorflow-lite-support sera ajouté quand le diagnostic IA sera implémenté
    // (conflit de namespace avec AGP 9 — en attente de mise à jour Google)
    // implementation(libs.tensorflow.lite.support)

    // === Coil (chargement d'images) ===
    implementation(libs.coil.compose)

    // === DataStore (préférences utilisateur) ===
    implementation(libs.androidx.datastore.preferences)

    // === Tests ===
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}