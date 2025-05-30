plugins {
    // Usa el plugin de Android Application sin versión (se toma la del classpath en el proyecto raíz)
    id("com.android.application")
    // Plugin de Kotlin para Android
    id("org.jetbrains.kotlin.android")
    // Plugin de Kotlin Serialization (se usa la versión definida en el classpath del proyecto raíz)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}

android {
    namespace = "com.example.divisaproviderapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.divisaproviderapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --- Jetpack Compose ---
    val composeBom = platform("androidx.compose:compose-bom:2023.06.01")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.1.0")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // --- Room ---
    implementation("androidx.room:room-ktx:2.5.1")
    // Si usas kapt para Room Compiler (descomenta la siguiente línea y aplica el plugin kapt en el top-level)
    // kapt("androidx.room:room-compiler:2.5.1")

    // --- WorkManager ---
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // --- Retrofit y Kotlinx Serialization ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    // --- AndroidX Core ---
    implementation("androidx.core:core-ktx:1.10.1")

    // --- Testing ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}