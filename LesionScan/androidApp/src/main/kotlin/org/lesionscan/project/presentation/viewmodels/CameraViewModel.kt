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
import org.lesionscan.project.domain.usecases.ClassifyLesionUseCase
import org.lesionscan.project.domain.usecases.ValidateFrameUseCase
import org.lesionscan.project.infrastructure.camera.FrameProcessor
import org.lesionscan.project.infrastructure.ml.MockModelRepository
import java.util.concurrent.Executors

data class FrameQualityState(
    val isLighting: Boolean = false,      // Green if true
    val isFocus: Boolean = false,         // Green if true
    val isDistance: Boolean = false,      // Green if true
    val isOptimal: Boolean = false,       // All three true
    val lightingLevel: Float = 0f,        // 0-1 (for progress bars if needed)
    val focusLevel: Float = 0f,           // 0-1
    val distanceLevel: Float = 0f         // 0-1
)

class CameraViewModel(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : ViewModel() {

    private val validateFrameUseCase = ValidateFrameUseCase()
    private val classifyLesionUseCase = ClassifyLesionUseCase(MockModelRepository())
    private val frameProcessor = FrameProcessor()

    private val _frameQualityState = MutableStateFlow(FrameQualityState())
    val frameQualityState: StateFlow<FrameQualityState> = _frameQualityState

    private var cameraProvider: ProcessCameraProvider? = null

    fun startCamera(previewView: PreviewView) {
        viewModelScope.launch {
            try {
                cameraProvider = ProcessCameraProvider.getInstance(context).let { future ->
                    future.get()  // Block until ready
                }

                val provider = cameraProvider ?: return@launch
                provider.unbindAll()

                // Preview use case
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

                // Image analysis for frame validation
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

                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                println("✓ Camera preview started")

            } catch (e: Exception) {
                println("✗ Camera start failed: ${e.message}")
            }
        }
    }

    private fun processFrame(imageProxy: androidx.camera.core.ImageProxy) {
        try {
            val width = imageProxy.width
            val height = imageProxy.height

            // Extract brightness from first plane (Y channel in YUV)
            val planes = imageProxy.planes
            val yPlane = planes[0]

            val buffer = yPlane.buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            // Calculate metrics from this frame
            val pixelArray = data.map { (it.toInt() and 0xFF) / 255f }.toFloatArray()
            val lighting = frameProcessor.calculateLighting(pixelArray)
            val sharpness = frameProcessor.calculateSharpness(pixelArray, width, height)
            val lesionArea = frameProcessor.calculateLesionArea(pixelArray)

            // Validate frame
            val frameQuality = validateFrameUseCase.execute(lighting, sharpness, lesionArea)

            // Update UI state with boolean flags
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
        cameraProvider?.unbindAll()
    }

    override fun onCleared() {
        stopCamera()
        super.onCleared()
    }
}