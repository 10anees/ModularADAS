// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.modular.adas.rearview"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.modular.adas.rearview"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0-MVP"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Note: Consider enabling R8 (true) for production to obfuscate and optimize ML pipeline execution
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
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Prevents APK packaging from compressing the YOLO model. 
    // If compressed, the OS cannot memory-map it to the GPU, causing severe CPU latency.
    androidResources {
        noCompress += "tflite"
    }
}

dependencies {
    // Core Android & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    // Jetpack Compose Framework
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation (Required for routing between Live Viewfinder and Calibration Matrix)
    val nav_version = "2.9.7"
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // Hilt Dependency Injection (Decouples Camera/ML instances from UI state)
    implementation("com.google.dagger:hilt-android:2.57.1")
    ksp("com.google.dagger:hilt-android-compiler:2.57.1")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel-compose:1.3.0")

    // CameraX - The Vision Pipeline (Using your tested v1.4.2)
    val camerax_version = "1.4.2"
    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")
    
    // Guava (Required to handle CameraX ListenableFutures gracefully)
    implementation("com.google.guava:guava:32.1.3-android")

    // ML Engine: TensorFlow Lite 
    // Includes Task Vision + GPU Delegates for Snapdragon 870 acceleration
    val tflite_version = "2.16.1"
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
    implementation("org.tensorflow:tensorflow-lite:$tflite_version")
    implementation("org.tensorflow:tensorflow-lite-gpu:$tflite_version")
    implementation("org.tensorflow:tensorflow-lite-gpu-api:$tflite_version")

    // Coroutines - Alert Logic
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // DataStore - Persistent Settings Storage
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}