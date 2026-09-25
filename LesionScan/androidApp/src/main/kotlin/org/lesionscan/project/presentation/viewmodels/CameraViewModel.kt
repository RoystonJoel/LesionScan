package org.lesionscan.project.presentation.viewmodels

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.lesionscan.project.domain.entities.LesionClassification
import org.lesionscan.project.domain.usecases.ClassifyLesionUseCase
import org.lesionscan.project.domain.usecases.ValidateFrameUseCase
import org.lesionscan.project.infrastructure.camera.FrameProcessor
import org.lesionscan.project.infrastructure.ml.MockModelRepository
import org.lesionscan.project.infrastructure.ml.ModelSandboxManager
import java.util.concurrent.Executors

data class FrameQualityState(
    val isLighting: Boolean = false,
    val isFocus: Boolean = false,
    val isDistance: Boolean = false,
    val isOptimal: Boolean = false,
    val lightingLevel: Float = 0f,
    val focusLevel: Float = 0f,
    val distanceLevel: Float = 0f
)

class CameraViewModel(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : ViewModel() {

    private val validateFrameUseCase = ValidateFrameUseCase()

    // real model:
    private val classifyLesionUseCase = ClassifyLesionUseCase(ModelSandboxManager(context))

    // mock - generates random:
    //private val classifyLesionUseCase = ClassifyLesionUseCase(MockModelRepository())
    private val frameProcessor = FrameProcessor()

    private val _frameQualityState = MutableStateFlow(FrameQualityState())
    val frameQualityState: StateFlow<FrameQualityState> = _frameQualityState

    private val _isFlashOn = MutableStateFlow(true)
    val isFlashOn: StateFlow<Boolean> = _isFlashOn

    private val _isClassifying = MutableStateFlow(false)
    val isClassifying: StateFlow<Boolean> = _isClassifying

    private val _classificationResult = MutableStateFlow<LesionClassification?>(null)
    val classificationResult: StateFlow<LesionClassification?> = _classificationResult

    private var currentFrameData: FloatArray? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraControl: androidx.camera.core.CameraControl? = null

    fun startCamera(previewView: PreviewView) {
        viewModelScope.launch {
            try {
                cameraProvider = ProcessCameraProvider.getInstance(context).let { future ->
                    future.get()
                }

                val provider = cameraProvider ?: return@launch
                provider.unbindAll()

                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()

                imageAnalysis.setAnalyzer(
                    Executors.newSingleThreadExecutor()
                ) { imageProxy ->
                    processFrame(imageProxy)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                val cameraInfo = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                cameraControl = cameraInfo.cameraControl
                enableFlash()

                println("✓ Camera preview started with flash enabled")

            } catch (e: Exception) {
                println("✗ Camera start failed: ${e.message}")
            }
        }
    }

    fun enableFlash() {
        viewModelScope.launch {
            try {
                cameraControl?.enableTorch(true)
                _isFlashOn.value = true
            } catch (e: Exception) {
                println("✗ Failed to enable flash: ${e.message}")
            }
        }
    }

    fun disableFlash() {
        viewModelScope.launch {
            try {
                cameraControl?.enableTorch(false)
                _isFlashOn.value = false
            } catch (e: Exception) {
                println("✗ Failed to disable flash: ${e.message}")
            }
        }
    }

    fun toggleFlash() {
        if (_isFlashOn.value) {
            disableFlash()
        } else {
            enableFlash()
        }
    }

    /**
     * Capture the current frame and run ML inference.
     * Called when user clicks the capture button.
     */
    fun captureAndClassify() {
        viewModelScope.launch {
            if (currentFrameData == null) {
                println("✗ No frame data available")
                return@launch
            }

            _isClassifying.value = true
            println("🔄 Starting inference...")

            try {
                val frame = currentFrameData!!
                val classification = classifyLesionUseCase.execute(frame)

                _classificationResult.value = classification
                println("✓ Classification complete: ${classification.riskLevel} (${String.format("%.2f", classification.riskScore)})")

            } catch (e: Exception) {
                println("✗ Inference failed: ${e.message}")
            } finally {
                _isClassifying.value = false
            }
        }
    }

    /**
     * Reset results and go back to camera
     */
    fun resetCapture() {
        _classificationResult.value = null
        currentFrameData = null
    }

    private fun processFrame(imageProxy: androidx.camera.core.ImageProxy) {
        try {
            val width = imageProxy.width
            val height = imageProxy.height

            val planes = imageProxy.planes
            val yPlane = planes[0]

            val buffer = yPlane.buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            val pixelArray = data.map { (it.toInt() and 0xFF) / 255f }.toFloatArray()

            // Store current frame for capture
            currentFrameData = pixelArray

            val lighting = frameProcessor.calculateLighting(pixelArray)
            val sharpness = frameProcessor.calculateSharpness(pixelArray, width, height)
            val lesionArea = frameProcessor.calculateLesionArea(pixelArray)

            val frameQuality = validateFrameUseCase.execute(lighting, sharpness, lesionArea)

            _frameQualityState.value = FrameQualityState(
                isLighting = frameQuality.isLightingGood,
                isFocus = frameQuality.isFocusSharp,
                isDistance = frameQuality.isDistanceCorrect,
                isOptimal = frameQuality.isOptimal,
                lightingLevel = frameQuality.lightingLevel,
                focusLevel = frameQuality.sharpnessScore,
                distanceLevel = frameQuality.lesionAreaPercentage
            )

        } catch (e: Exception) {
            println("✗ Frame processing failed: ${e.message}")
        } finally {
            imageProxy.close()
        }
    }

    fun stopCamera() {
        disableFlash()
        cameraProvider?.unbindAll()
    }

    override fun onCleared() {
        stopCamera()
        super.onCleared()
    }
}