 
# LesionScan App

On-device melanoma detection app built with Kotlin Multiplatform (KMP) 


## Run Locally

1. Clone the project inside android studio or IntelliJ IDEA

```bash
  git clone https://github.com/RoystonJoel/LesionScan.git
```

2. Install the following plugins:

- Android
- Kotlin Multiplatform
- Compose Multiplatform
- Gradle (if it isn't already installed)

No separate JDK install is needed — android studio or IntelliJ IDEA bundled JDK is sufficient.

3. Setup an android device via Device Manager or plug in your android device via USB ( make sure USB debugging is on via developer options )

4. Select AndroidApp from the Run/Debug configuration to run the app

5. To be able to run tests make sure to open the project inside LesionScan\LesionScan, so the IDE can detect the Gradle files.




## Features

- On-Device ML Inference: Uses a compressed CoreML or TensorFlow Lite model running completely offline on the device.   
- Camera Guidance Overlay: An interactive camera UI that guides the user to position the camera at the perfect distance, lighting, and focus level to get an optimal image for classification.  

## Running Tests

The project has three kinds of tests. Make sure you've opened the project at `LesionScan/LesionScan` (per step 5 above) so Gradle can detect them.

**Shared JVM unit tests** (no device needed — `./gradlew testDebugUnitTest` or run via IDE):
- [FrameProcessorTest.kt](https://github.com/RoystonJoel/LesionScan/blob/main/LesionScan/shared/src/androidHostTest/kotlin/org/lesionscan/project/infrastructure/camera/FrameProcessorTest.kt) — lighting, sharpness and lesion-area calculations
- [SharedLogicAndroidHostTest.kt](https://github.com/RoystonJoel/LesionScan/blob/main/LesionScan/shared/src/androidHostTest/kotlin/org/lesionscan/project/SharedLogicAndroidHostTest.kt)

**Common (cross-platform) unit tests** (no device needed):
- [ValidateFrameUseCaseTest.kt](https://github.com/RoystonJoel/LesionScan/blob/main/LesionScan/shared/src/commonTest/kotlin/org/lesionscan/project/project/domain/usecases/ValidateFrameUseCaseTest.kt) — optimal, dark, overexposed and poor-distance frame conditions
- [LesionClassificationTest.kt](https://github.com/RoystonJoel/LesionScan/blob/main/LesionScan/shared/src/commonTest/kotlin/org/lesionscan/project/project/domain/entities/LesionClassificationTest.kt) — risk-level mapping
- [FrameQualityStateTest.kt](https://github.com/RoystonJoel/LesionScan/blob/main/LesionScan/shared/src/commonTest/kotlin/org/lesionscan/project/presentation/viewmodels/FrameQualityStateTest.kt)
- [SharedCommonTest.kt](https://github.com/RoystonJoel/LesionScan/blob/main/LesionScan/shared/src/commonTest/kotlin/org/lesionscan/project/SharedCommonTest.kt)

**Instrumented tests** (requires a connected Android device or emulator — `./gradlew connectedAndroidTest`):
- [ModelSandboxManagerIntegrationTest.kt](https://github.com/RoystonJoel/LesionScan/blob/main/LesionScan/shared/src/androidDeviceTest/kotlin/org/lesionscan/project/infrastructure/ml/ModelSandboxManagerIntegrationTest.kt) — model accuracy, confidence scores, and inference latency against bundled melanoma/non-melanoma test images


## Architecture

[![Architecture diagram of roystonjoel/lesionscan](https://gitdiagram.com/roystonjoel/lesionscan/diagram.png)](https://gitdiagram.com/roystonjoel/lesionscan?utm_source=readme&utm_medium=picture)

