 
# LesionScan App

On-device melanoma detection app built with Kotlin Multiplatform (KMP) for preliminary detection of skin lesions, built to help a user decide whether a mole or mark on their skin is worth a dermatologist visit. 


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

## User stories cards

| **Card Name** | **Labels** | **Definition of Done** | **Completed**       |
| --- | --- | --- |---------------------|
| US-01 As a user, I want the camera to tell me when to move closer or further away, so that the mole is at the right distance without me guessing. | 3 story points (pink), EPIC: Camera guidance (red_dark), Should Haves (orange) | The camera UI successfully calculates distance and continuously displays instructions to move closer or further away without requiring user guesswork. | 14th september 2026 |
| US-02 As a user, I want to be told when the lighting is too dark or too bright, so that the photo has the colour accuracy the assessment needs. | 2 story points (green), EPIC: Camera guidance (red_dark), Should Haves (orange) | The app accurately evaluates environmental lighting and triggers an warning when the scene is too dark or too bright to achieve color accuracy. | 14th september 2026 |
| US-03 As a user, I want to be told when the image is out of focus, so that I know to hold still until the lesion is sharp. | 3 story points (pink), EPIC: Camera guidance (red_dark), Should Haves (orange) | The camera feed accurately checks focus and displays a warning when the image is blurred, clearing the warning only when the lesion is sharp. | 14th September 2026 |
| US-04 As a user, I want there to be an overlay on screen that shows me where the middle of the screen is exactly so I know where I should position the camera. | 2 story points (green), EPIC: Camera guidance (red_dark), Must Haves (blue) | A visual circle is permanently rendered in the exact center of the screen to explicitly guide the user's camera positioning. | 14th September 2026 |
| US-05 As a User, Once the Lighting, Distance is optimal I want to be able to capture the Skin Lesion. | 2 story points (green), EPIC: Camera guidance (red_dark), Must Haves (blue) | The system reliably allows image capture only after validating that focus, lighting and distance metrics are optimal. | 14th September 2026 |
| US-06 As a user the classified Skin lesion should be put into a risk level grouping, to help inform my dermatologist to make a final decision. | 3 story points (pink), EPIC: Lesion Classification (purple), Should Haves (orange) | The app successfully translates the machine learning model's raw classification into a distinct risk level grouping for the user. | 05th October 2026   |
| US-07 As a user I want my skin lesion to be classified using a machine learning model , so that I know if I should consult with a dermatologist. | 8 story points (purple_dark), EPIC: Lesion Classification (purple), Must Haves (blue) | The captured image is successfully routed to the integrated machine learning model, which returns a valid classification to help the user decide if they should consult a dermatologist. | 25th September 2026 |
| US-08 As a user the classified Skin lesion to have a risk score, to help inform my dermatologist to make a final decision. | 2 story points (green), Could Haves (yellow), EPIC: Lesion Classification (purple) | The UI displays a calculated risk score associated with the classified skin lesion to inform the dermatologist's final decision. | 10th October 2026   |
| US-09 As a user I want to be able to be able to go back to the previous screen to scan another lesion | 2 story points (green), EPIC: Lesion Classification (purple), Must Haves (blue) | The UI provides a fully functional back navigation action that successfully resets the camera state and allows the user to easily scan a new lesion. | 10th October 2026   |

