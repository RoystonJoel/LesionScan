import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    
    android {
       namespace = "org.lesionscan.project.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)

            // TensorFlow Lite core
            implementation("org.tensorflow:tensorflow-lite:2.17.0")

            // TensorFlow Lite Support Library (provides TensorBuffer, DataType, etc.)
            //implementation("org.tensorflow:tensorflow-lite-support:0.5.0")

            // GPU delegate (optional, for hardware acceleration)
            //implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")

            // NNAPI delegate (uses device's NPU/GPU if available)
            //implementation("org.tensorflow:tensorflow-lite-nnapi:2.14.0")



            // CameraX (camera pipeline)
            implementation("androidx.camera:camera-core:1.3.0")
            implementation("androidx.camera:camera-camera2:1.3.0")
            implementation("androidx.camera:camera-lifecycle:1.3.0")
            implementation("androidx.camera:camera-view:1.3.0")

            // Compose integration
            implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

        }
        commonTest.dependencies {
            //implementation(kotlin("test"))
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

configurations.all {
    // This forces Gradle to completely exclude the colliding api package globally
    exclude(group = "org.tensorflow", module = "tensorflow-lite-api")
}