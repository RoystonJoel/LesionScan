 
# LesionScan App

On-device melanoma detection app built with Kotlin Multiplatform (KMP) 


## Run Locally

1. Clone the project inside android studio or IntelJIDEA

```bash
  git clone https://github.com/RoystonJoel/LesionScan.git
```

2. Install the following plugins:

- Android
- Kotlin Multiplatform
- Gradle

3. Setup an android device via Device Manager or plugin your android device via USB ( make sure USB debugging is on via developer options )

4. Select AndroidApp from the Run/Debug configuration to run the app




## Features

- On-Device ML Inference: Uses a compressed CoreML or TensorFlow Lite model running completely offline on the device.   
- Camera Guidance Overlay: An interactive camera UI that guides the user to position the camera at the perfect distance, lighting, and focus level to get an optimal image for classification.  



## Architecture

[![Architecture diagram of roystonjoel/lesionscan](https://gitdiagram.com/roystonjoel/lesionscan/diagram.png)](https://gitdiagram.com/roystonjoel/lesionscan?utm_source=readme&utm_medium=picture)

